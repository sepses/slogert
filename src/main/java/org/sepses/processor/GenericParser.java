package org.sepses.processor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.sepses.logline.LogLine;
import org.sepses.helper.Template;
import org.sepses.helper.Utility;
import org.sepses.rdf.Slogert;
import org.sepses.yaml.Config;
import org.sepses.yaml.ConfigParameter;
import org.sepses.yaml.InternalLogType;
import org.sepses.yaml.YamlFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class GenericParser implements Parser {

    private static final Logger log = LoggerFactory.getLogger(GenericParser.class);

    private static final String[] TEMPLATE_LOGPAI = { "EventId", "EventTemplate", "Occurrences" };

    private final String BASE_INSTANCE = "id:LogEntry_";
    private final Map<String, Template> hashTemplates;
    private final Config config;
    private final HashMap<String, ConfigParameter> parameterMap = new HashMap<>();
    private final Constructor<?> constructor;

    public GenericParser(Config config) throws IOException, ReflectiveOperationException {

        // init regexNER & OTTR template
        YamlFunction.constructRegexNer(config);
        YamlFunction.constructOttrTemplate(config);

        // initialization
        hashTemplates = new HashMap<>();
        this.config = config;

        // add NER and non-NER parameterList
        config.nerParameters.forEach(item -> parameterMap.put(item.id, item));
        config.nonNerParameters.forEach(item -> parameterMap.put(item.id, item));

        // initialize reflective class for logline
        String clazzString = config.internalLogType.classPath;
        Class<?> clazz = Class.forName(clazzString);
        constructor = clazz.getConstructor(CSVRecord.class, InternalLogType.class);

        // parsing & update template
        createOrUpdateTemplate();
    }

    /**
     * *** extract additional hashTemplates from input log (data+hashTemplates) if possible
     *
     * @param logpaiStructure
     * @param inputData
     */
    private void extractTemplate(Iterable<CSVRecord> logpaiStructure, List<LogLine> inputData) {

        // *** Annotate template nerParameters
        for (CSVRecord templateCandidate : logpaiStructure) {
            try {
                String logpaiEventId = templateCandidate.get(TEMPLATE_LOGPAI[0]);
                String logpaiTemplate = templateCandidate.get(TEMPLATE_LOGPAI[1]);
                String hashCandidate = Utility.createHash(logpaiTemplate);

                if(hashCandidate.equalsIgnoreCase("c56033738cc8a503610c9a743687746ab3d59ca61a0b8564c5323b4bb4f56b6e")) {
                    log.info(hashCandidate);
                }

                if (!hashTemplates.containsKey(hashCandidate)) {
                    // *** Generate new template
                    log.info(hashCandidate);
                    for (LogLine logLine : inputData) {
                        // ** Find logLine with the corresponding template
                        if (logpaiEventId.equals(logLine.getLogpaiEventId())) {
                            if (!hashCandidate.equals(logLine.getTemplateHash())) {
                                log.warn("logpai-id: " + logpaiEventId + " template is not consistent!");
                                hashCandidate = logLine.getTemplateHash();
                            }
                            Template template = new Template(logpaiTemplate, hashCandidate, logLine, config);
                            hashTemplates.put(hashCandidate, template);
                            break;
                        }
                    }
                }
            } catch (NoSuchAlgorithmException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override public void createOrUpdateTemplate() throws IOException {
        String namedGraph = Slogert.NS_INSTANCE + config.logType;
        Dataset templateDS = DatasetFactory.createTxnMem();

        templateDS.begin(ReadWrite.WRITE);
        log.debug("read and collect input logpai structure");
        try {

            // *** load existing hashTemplates
            File templateRDF = new File(config.templateRdf);
            log.debug("file: " + templateRDF.getAbsolutePath());
            if (templateRDF.isFile()) {
                log.debug("isFile");
                FileInputStream fis = new FileInputStream(config.templateRdf);
                log.debug("fis initialization");
                RDFDataMgr.read(templateDS, fis, Lang.TRIG);
                fis.close();

                log.debug("reading in fis");
                if (templateDS.containsNamedModel(namedGraph)) {
                    log.debug("contain named graph");
                    hashTemplates.putAll(Template.fromModel(templateDS.getNamedModel(namedGraph), config));
                    log.debug("read to templates");
                }
            }

            // *** read and collect input logpai structure
            Reader templateReader = new FileReader(Paths.get(config.logTemplate).toFile());
            Iterable<CSVRecord> inputTemplates = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(templateReader);
            log.debug("read and collect input logpai structure");

            // *** read and collect input logpai data
            Reader dataReader = new FileReader(Paths.get(config.logData).toFile());
            Iterable<CSVRecord> inputData = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(dataReader);
            List<LogLine> logLines = new ArrayList<>();
            inputData.forEach(inputRow -> logLines.add(createLogLine(inputRow)));
            log.debug("read and collect input logpai data");

            // *** derive hashTemplates
            extractTemplate(inputTemplates, logLines);
            log.debug("derive hashTemplates");

            // *** close readers
            templateReader.close();
            dataReader.close();
            log.debug("close readers");

            // *** setup DS
            if (templateDS.getDefaultModel().isEmpty()) {
                Model defaultModel = ModelFactory.createDefaultModel();
                config.ottrNS.stream().forEach(ns -> defaultModel.setNsPrefix(ns.prefix, ns.uri));
                Resource resource = defaultModel.createResource(Slogert.getURI());
                defaultModel.add(resource, RDF.type, OWL.Ontology);

                templateDS.setDefaultModel(defaultModel);
                templateDS.getDefaultModel().setNsPrefixes(defaultModel.getNsPrefixMap());
            }
            log.debug("setup ds");

            // *** write templates
            Model model = ModelFactory.createOntologyModel();
            hashTemplates.values().stream().forEach(item -> model.add(item.toModel()));
            templateDS.replaceNamedModel(namedGraph, model);
            templateDS.commit();
            log.debug("commits");

            FileOutputStream fos = new FileOutputStream(config.templateRdf);
            RDFDataMgr.write(fos, templateDS, RDFFormat.TRIG);
            log.debug("write templates");

        } catch (Exception e) {
            log.error("*** Jena Dataset error ***");
            log.error(e.getMessage());
        } finally {
            templateDS.end();
        }
    }

    @Override public void generateOttrMap() throws IOException {
        StringBuilder sb = new StringBuilder();

        // *** load template
        InputStream is = new FileInputStream(config.targetOttr);
        try {
            String baseTemplate = IOUtils.toString(is, Charset.defaultCharset());
            sb.append(baseTemplate);
            sb.append(System.lineSeparator()).append(System.lineSeparator());

            hashTemplates.values().stream().forEach(template -> {
                sb.append(template.generateOttrTemplate());
                sb.append(System.lineSeparator());
            });
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        Utility.writeToFile(sb.toString(), config.targetTemplate);
    }

    @Override public void parseLogpaiData() throws IOException {

        // *** read and collect input logpai data
        Reader dataReader = new FileReader(Paths.get(config.logData).toFile());
        Iterable<CSVRecord> inputData = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(dataReader);
        List<LogLine> logLines = new ArrayList<>();
        inputData.forEach(inputRow -> logLines.add(createLogLine(inputRow)));
        dataReader.close();

        StringBuilder sb = new StringBuilder();
        config.ottrNS.forEach(ns -> {
            sb.append("@prefix " + ns.prefix + ": <" + ns.uri + "> .").append(System.lineSeparator());
        });
        sb.append(System.lineSeparator());

        logLines.forEach(logLine -> {
            Template template = hashTemplates.get(logLine.getTemplateHash());

            sb.append(template.ottrId);
            sb.append("(").append(BASE_INSTANCE).append(UUID.randomUUID()).append(",\"");
            sb.append(logLine.getDevice()).append("\",\"");
            sb.append(logLine.getDateTime()).append("\",\"");
            sb.append(Utility.cleanContent(logLine.getContent())).append("\",\""); // necessary due to OTTR issue
            sb.append(logLine.getTemplateHash()).append("\",\"");

            // log-specific params
            config.internalLogType.components.stream().forEach(item -> {
                String value = logLine.getSpecialParameters().get(item.column);
                sb.append(value).append("\",\"");
            });

            for (int i = 0; i < template.parameters.size(); i++) {
                String paramString = logLine.getParameters().get(i);
                String paramType = template.parameters.get(i);
                ConfigParameter parameter = parameterMap.get(paramType);
                sb.append(Utility.cleanContent(paramString)).append("\",\"");
            }

            sb.delete(sb.length() - 2, sb.length());
            sb.append(") .").append(System.lineSeparator());
        });

        Utility.writeToFile(sb.toString(), config.targetData);
    }

    /**
     * creation of a logline
     * TODO: Check and update!
     *
     * @param record
     * @return
     * @throws NoSuchAlgorithmException
     */
    private LogLine  createLogLine(CSVRecord record) {
        LogLine logline = null;

        try {
            logline = (LogLine) constructor.newInstance(record, config.internalLogType);
        } catch (ReflectiveOperationException e) {
            log.error("reflection error");
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return logline;
    }
}
