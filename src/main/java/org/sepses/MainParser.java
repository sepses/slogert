package org.sepses;

import org.apache.commons.cli.*;
import org.sepses.processor.GenericParser;
import org.sepses.processor.Parser;
import org.sepses.yaml.Config;
import org.sepses.yaml.InternalConfig;
import org.sepses.yaml.InternalLogType;
import org.sepses.yaml.YamlFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.util.NoSuchElementException;

public class MainParser {

    private static final Logger log = LoggerFactory.getLogger(MainParser.class);

    private static final String INTERNAL_CONFIG = "slogert.yaml";
    private static Config config;
    private static InternalConfig internalConfig;

    public static void main(String[] args) throws ParseException, IOException {

        // loading internal configuration file.
        InputStream internalConfigIS = MainParser.class.getClassLoader().getResourceAsStream(INTERNAL_CONFIG);
        Yaml yaml = new Yaml(new Constructor(InternalConfig.class));
        internalConfig = yaml.load(internalConfigIS);
        internalConfigIS.close();

        Options options = new Options();
        options.addRequiredOption("c", "config.yaml", true, "SLOGERT I/O Config");
        options.addRequiredOption("t", "template.yaml", true, "SLOGERT Regex & Namespaces");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String configFileString = cmd.getOptionValue("c").trim();
        File configFile = new File(configFileString);
        String templateFileString = cmd.getOptionValue("t").trim();
        File templateFile = new File(templateFileString);

        yaml = new Yaml(new Constructor(Config.class));

        // check whether the parameterList are files
        if (!configFile.isFile() || !templateFile.isFile())
            return;

        InputStream configIS = new FileInputStream(configFile);
        InputStream templateIS = new FileInputStream(templateFile);
        InputStream is = new SequenceInputStream(configIS, templateIS);
        config = yaml.load(is);

        long start = System.currentTimeMillis();
        long end;

        try {
            // get internalLogType from the known log type list
            InternalLogType internalLogType =
                    internalConfig.logTypes.stream().filter(item -> item.id.equals(config.logType)).findFirst().get();
            // set internal logtype; throw error when not found
            config.internalLogType = internalLogType;
            // add specific NS to the config NS list.
            config.ottrNS.addAll(internalLogType.ottrNS);

            YamlFunction.convertConfigToOwl(config);

            log.info("*** start log parser ***");
            Parser unixParser = new GenericParser(config);
            unixParser.generateOttrMap();
            unixParser.parseLogpaiData();
            log.info("*** log parser processing finished ***");

        } catch (NoSuchElementException e) {
            log.error("*** Unsupported log type ***");
            log.error(e.getMessage());

        } catch (ReflectiveOperationException e) {
            log.error("*** Reflection error ***");
            log.error(e.getMessage());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("java -jar exe/lutra.jar --library ").append(config.targetTemplate)
                .append(" --libraryFormat stottr --inputFormat stottr ").append(config.targetData)
                .append(" --mode expand --fetchMissing > ").append(config.targetTurtle);

        end = System.currentTimeMillis();
        log.info("Transformation process finished in " + (end - start) + " milliseconds");
        log.info("Execute Lutra with the following commands: " + sb.toString());

        System.gc();

    }

}
