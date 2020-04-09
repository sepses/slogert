package org.sepses.yaml;

import org.apache.commons.io.IOUtils;
import org.sepses.helper.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

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

        log.info("resulted rule: " + sb.toString());
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
            sb.append("id:").append(component.column).append("[ottr:IRI ?id, ").append(component.ottr.ottrType)
                    .append(" ?value] :: {").append(System.lineSeparator());
            sb.append("\t ottr:Triple(?id, ").append(component.ottr.ottrProperty).append(", ?value)")
                    .append(System.lineSeparator());
            sb.append("} .").append(System.lineSeparator());
        });

        // add params for ner nerParameters
        config.nerParameters.forEach(parameter -> {
            sb.append("# OTTR ner parameter: ").append(parameter.id).append(System.lineSeparator());
            sb.append("id:").append(parameter.id).append("[ottr:IRI ?id, ").append(parameter.ottr.ottrType)
                    .append(" ?value] :: {").append(System.lineSeparator());
            sb.append("\t ottr:Triple(?id, ").append(parameter.ottr.ottrProperty).append(", ?value)")
                    .append(System.lineSeparator());
            sb.append("} .").append(System.lineSeparator());
        });

        // add params for non-ner nerParameters
        config.nonNerParameters.forEach(parameter -> {
            sb.append("# OTTR non-ner parameter: ").append(parameter.id).append(System.lineSeparator());
            sb.append("id:").append(parameter.id).append("[ottr:IRI ?id, ").append(parameter.ottr.ottrType)
                    .append(" ?value] :: {").append(System.lineSeparator());
            sb.append("\t ottr:Triple(?id, ").append(parameter.ottr.ottrProperty).append(", ?value)")
                    .append(System.lineSeparator());
            sb.append("} .").append(System.lineSeparator());
        });
        sb.append(System.lineSeparator());
        sb.append("# *** LogPai-Generated Templates ***");
        sb.append(System.lineSeparator());

        log.info("resulted stottr: \n" + sb.toString());
        Utility.writeToFile(sb.toString(), config.targetOttr);
    }

}
