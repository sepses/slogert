package org.sepses.processor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.sepses.helper.LogLine;
import org.sepses.helper.Template;
import org.sepses.helper.UnixLogLine;
import org.sepses.helper.Utility;
import org.sepses.yaml.Config;
import org.sepses.yaml.NameSpace;
import org.sepses.yaml.Parameter;
import org.sepses.yaml.YamlFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class UnixParser implements Parser {

    private static final Logger log = LoggerFactory.getLogger(UnixParser.class);

    private static final String[] TEMPLATE_LOGPAI = { "EventId", "EventTemplate", "Occurrences" };

    private final Map<String, Template> hashTemplates;
    private final Config config;
    private final HashMap<String, Parameter> parameterMap = new HashMap<>();

    public UnixParser(Config config) throws IOException {
        // init regexNER & OTTR template
        YamlFunction.constructRegexNer(config);
        YamlFunction.constructOttrTemplate(config);

        // initialization
        hashTemplates = new HashMap<>();
        this.config = config;
        config.parameters.forEach(parameter -> {
            parameterMap.put(parameter.label, parameter);
        });
        createOrUpdateTemplate();
    }

    /**
     * *** extract additional hashTemplates from input log (data+hashTemplates) if possible
     */
    public void extractTemplate(Iterable<CSVRecord> logpaiStructure, List<LogLine> inputData) {

        // *** Annotate template parameters
        for (CSVRecord templateCandidate : logpaiStructure) {
            try {
                String logpaiEventId = templateCandidate.get(TEMPLATE_LOGPAI[0]);
                String logpaiTemplate = templateCandidate.get(TEMPLATE_LOGPAI[1]);
                String hashCandidate = Utility.createHash(logpaiTemplate);

                if (!hashTemplates.containsKey(hashCandidate)) {
                    // *** Generate new template
                    for (LogLine logLine : inputData) {
                        // ** Find logLine with the corresponding template
                        if (logpaiEventId.equals(logLine.getLogpaiEventId())) {
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

        // *** load existing hashTemplates
        if (!config.isOverride) {
            File logpaiTemplate = new File(getClass().getClassLoader().getResource(config.baseTemplate).getFile());
            Reader reader = new FileReader(logpaiTemplate);
            Iterable<CSVRecord> readerIterator = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
            hashTemplates.putAll(Utility.loadTemplates(readerIterator, config));
            reader.close();
        }

        // *** read and collect input logpai structure
        Reader templateReader = new FileReader(Paths.get(config.logTemplate).toFile());
        Iterable<CSVRecord> inputTemplates = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(templateReader);
        // *** read and collect input logpai data
        Reader dataReader = new FileReader(Paths.get(config.logData).toFile());
        Iterable<CSVRecord> inputData = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(dataReader);
        List<LogLine> logLines = new ArrayList<>();
        inputData.forEach(inputRow -> logLines.add(UnixLogLine.getInstance(inputRow)));

        // *** derive hashTemplates
        extractTemplate(inputTemplates, logLines);
        templateReader.close();
        dataReader.close();
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
                sb.append(template.ottrTemplate);
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
        inputData.forEach(inputRow -> logLines.add(UnixLogLine.getInstance(inputRow)));
        dataReader.close();

        StringBuilder sb = new StringBuilder();
        config.ottrNS.forEach(ns -> {
            sb.append("@prefix " + ns.prefix + ": <" + ns.uri + "> .").append(System.lineSeparator());
        });
        parameterMap.values().forEach(parameter -> {
            NameSpace nameSpace = parameter.ottr.namespace;
            sb.append("@prefix " + nameSpace.prefix + ": <" + nameSpace.uri + "> .").append(System.lineSeparator());
        });
        sb.append(System.lineSeparator());

        logLines.forEach(logLine -> {
            Template template = hashTemplates.get(logLine.getTemplateHash());

            sb.append(template.ottrId);
            sb.append("(").append(Template.BASE_OTTR_ID).append(UUID.randomUUID()).append(",\"");
            sb.append(logLine.getDateTime()).append("\",\"");
            sb.append(Utility.cleanContent(logLine.getContent())).append("\",\"");
            sb.append(logLine.getTemplateHash()).append("\",\"");

            for (int i = 0; i < template.parameters.size(); i++) {
                String paramString = logLine.getParameters().get(i);
                String paramType = template.parameters.get(i);
                Parameter parameter = parameterMap.get(paramType);

                if (paramType.equals(Template.UNKNOWN_PARAMETER) || !parameter.ottr.ottrType
                        .equals(Utility.OTTR_IRI)) {
                    sb.append(Utility.cleanContent(paramString)).append("\",\"");
                } else {
                    sb.delete(sb.length() - 1, sb.length());
                    paramString = parameter.ottr.namespace.prefix + ":" + Utility.cleanUriContent(paramString);
                    sb.append(paramString).append(",\"");
                }
            }

            sb.delete(sb.length() - 2, sb.length());
            sb.append(") .").append(System.lineSeparator());
        });

        Utility.writeToFile(sb.toString(), config.targetData);
    }

    @Override public Map<String, Template> getHashTemplates() {
        return hashTemplates;
    }
}
