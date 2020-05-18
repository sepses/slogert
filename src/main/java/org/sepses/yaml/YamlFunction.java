package org.sepses.yaml;

import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.sepses.helper.Utility;
import org.sepses.rdf.Slogert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 * YAML-related functions
 */
public class YamlFunction {

    public static final String UNIX_BASE = "unix-base.stottr";
    public static final String NER_BASE = "ner-base.rules";

    private static final Logger log = LoggerFactory.getLogger(YamlFunction.class);

    public static void initialize(Config config) throws IOException {
        constructRegexNer(config);
        constructOttrTemplate(config);
    }

    /**
     * Constructing a Stanford RegexNER from the configuration file.
     *
     * @param config
     * @throws IOException
     */
    public static void constructRegexNer(Config config) throws IOException {

        StringBuilder sb = new StringBuilder();
        InputStream nerIS = YamlFunction.class.getClassLoader().getResourceAsStream(NER_BASE);
        sb.append(IOUtils.toString(nerIS, Charset.defaultCharset()));

        config.nerParameters.forEach(parameter -> {
            sb.append("{ ruleType: \"tokens\", ");
            sb.append("pattern: ").append(parameter.regexNer.pattern).append(", ");
            sb.append("action: ").append(parameter.regexNer.action).append(", ");
            sb.append("result: \"").append(parameter.label).append("\" }");
            sb.append(System.lineSeparator());
        });

        //        log.info("resulted rule: " + sb.toString());
        Utility.writeToFile(sb.toString(), config.targetNer);
    }

    /**
     * Constructing an OTTR template from the configuration file.
     *
     * @param config
     * @throws IOException
     */
    public static void constructOttrTemplate(Config config) throws IOException {

        // add NSs
        StringBuilder sb = new StringBuilder();
        config.ottrNS.forEach(ns -> {
            sb.append("@prefix ").append(ns.prefix).append(": <").append(ns.uri).append("> .")
                    .append(System.lineSeparator());
        });
        sb.append(System.lineSeparator());

        // add default logLine
        InputStream nerIS = YamlFunction.class.getClassLoader().getResourceAsStream(UNIX_BASE);
        sb.append(IOUtils.toString(nerIS, Charset.defaultCharset()));

        // *** handle log-type specific params
        InternalLogType iLogType = config.internalLogType;
        iLogType.components.stream().forEach(component -> {
            sb.append("# log-specific OTTR parameter: ").append(component.column).append(System.lineSeparator());
            sb.append("id:").append(component.column).append("[ottr:IRI ?id, xsd:string ?value] :: {")
                    .append(System.lineSeparator());
            sb.append("\t ottr:Triple(?id, ").append(component.ottr.ottrProperty).append(", ?value)")
                    .append(System.lineSeparator());
            sb.append("} .").append(System.lineSeparator());
        });

        // add params for ner nerParameters
        config.nerParameters.forEach(parameter -> {
            sb.append("# OTTR ner parameter: ").append(parameter.id).append(System.lineSeparator());
            sb.append("id:").append(parameter.id).append("[ottr:IRI ?id, xsd:string ?value] :: {")
                    .append(System.lineSeparator());
            sb.append("\t ottr:Triple(?id, ").append(parameter.ottr.ottrProperty).append(", ?value)")
                    .append(System.lineSeparator());
            sb.append("} .").append(System.lineSeparator());
        });

        // add params for non-ner nerParameters
        config.nonNerParameters.forEach(parameter -> {
            sb.append("# OTTR non-ner parameter: ").append(parameter.id).append(System.lineSeparator());
            sb.append("id:").append(parameter.id).append("[ottr:IRI ?id, xsd:string ?value] :: {")
                    .append(System.lineSeparator());
            sb.append("\t ottr:Triple(?id, ").append(parameter.ottr.ottrProperty).append(", ?value)")
                    .append(System.lineSeparator());
            sb.append("} .").append(System.lineSeparator());
        });
        sb.append(System.lineSeparator());
        sb.append("# *** LogPai-Generated Templates ***");
        sb.append(System.lineSeparator());

        //        log.info("resulted stottr: \n" + sb.toString());
        Utility.writeToFile(sb.toString(), config.targetOttr);
    }

    public static void convertConfigToOwl(Config config) throws FileNotFoundException {

        InputStream is = YamlFunction.class.getClassLoader().getResourceAsStream("slogert-v1.0.0.ttl");
        Model defaultModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(defaultModel, is, Lang.TURTLE);

        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefixes(defaultModel.getNsPrefixMap());

        String paramPrefix = Slogert.NS_INSTANCE + "param_";
        String configPrefix = Slogert.NS_INSTANCE + "config_";

        File file = new File(config.slogertTrig);
        File targetTrig = new File(config.targetTurtle);
        String trigString = targetTrig.getName();
        Resource namedGraph = model.createResource(Slogert.NS_INSTANCE + trigString);

        config.nerParameters.stream().forEach(param -> {
            Resource sid = model.createResource(paramPrefix + param.id);
            model.add(sid, RDF.type, Slogert.NerParameter);
            model.add(sid, RDFS.label, param.label);
            model.add(sid, RDFS.comment, param.comment);
            model.add(sid, Slogert.id, param.id);
            model.add(sid, Slogert.example, param.example);
            model.add(sid, Slogert.hasProperty, stringToResource(model, param.ottr.ottrProperty));
            model.add(sid, Slogert.hasSuggestedRange, stringToResource(model, param.ottr.ottrType));
            model.add(sid, Slogert.annotateAction, param.regexNer.action);
            model.add(sid, Slogert.regexPattern, param.regexNer.pattern);
        });

        config.nonNerParameters.stream().forEach(param -> {
            Resource sid = model.createResource(paramPrefix + param.id);
            model.add(sid, RDF.type, Slogert.InputParameter);
            model.add(sid, RDFS.label, param.label);
            model.add(sid, RDFS.comment, param.comment);
            model.add(sid, Slogert.id, param.id);
            model.add(sid, Slogert.example, param.example);
            model.add(sid, Slogert.hasProperty, stringToResource(model, param.ottr.ottrProperty));
            model.add(sid, Slogert.hasSuggestedRange, stringToResource(model, param.ottr.ottrType));
            model.add(sid, Slogert.regexPattern, param.regexNer.pattern);
        });

        InternalLogType logType = config.internalLogType;
        Resource logConfig = model.createResource(configPrefix + logType.id);
        // internal
        model.add(logConfig, RDF.type, Slogert.LogConfig);
        model.add(logConfig, Slogert.id, logType.id);
        model.add(logConfig, Slogert.hasClass, stringToResource(model, logType.clazz));
        model.add(logConfig, Slogert.classPath, logType.classPath);
        model.add(logConfig, Slogert.csvHeader, logType.header);
        // common config
        model.add(logConfig, Slogert.hasTargetClass, stringToResource(model, config.logName));
        model.add(logConfig, Slogert.turtleOutput, trigString);

        logType.components.stream().forEach(param -> {
            String pid = logType.id + "." + param.column;
            Resource sid = model.createResource(paramPrefix + pid);
            model.add(sid, RDF.type, Slogert.InternalParameter);
            model.add(sid, RDFS.label, pid);
            model.add(sid, Slogert.id, pid);
            model.add(sid, Slogert.csvColumn, param.columnNr);
            model.add(sid, Slogert.csvColumnName, param.column);
            model.add(sid, Slogert.hasProperty, stringToResource(model, param.ottr.ottrProperty));
            model.add(sid, Slogert.hasSuggestedRange, stringToResource(model, param.ottr.ottrType));
            model.add(sid, Slogert.hasLogConfig, logConfig);
        });

        Dataset dataset = DatasetFactory.create();
        if (Files.exists(file.toPath())) {
            RDFDataMgr.read(dataset, config.slogertTrig, Lang.TRIG);
            if (dataset.containsNamedModel(namedGraph.getURI())) {
                dataset.removeNamedModel(namedGraph.getURI());
            }
        } else {
            dataset.setDefaultModel(defaultModel);
        }
        dataset.addNamedModel(namedGraph.getURI(), model);
        RDFDataMgr.write(new FileOutputStream(file), dataset, Lang.TRIG);

        dataset.close();
        model.close();
        defaultModel.close();

    }

    private static Resource stringToResource(Model model, String label) {
        String[] labels = label.split(":");
        Resource resource = model.createResource(model.getNsPrefixURI(labels[0]) + labels[1]);
        return resource;
    }

}
