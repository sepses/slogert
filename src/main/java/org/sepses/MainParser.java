package org.sepses;

import org.apache.commons.cli.*;
import org.sepses.processor.Parser;
import org.sepses.processor.UnixParser;
import org.sepses.yaml.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainParser {

    private static final Logger log = LoggerFactory.getLogger(MainParser.class);

    private static Config config;

    public static void main(String[] args) throws ParseException, IOException {

        Options options = new Options();
        options.addRequiredOption("c", "config.yaml", true, "SLOGERT I/O Config");
        options.addRequiredOption("t", "template.yaml", true, "SLOGERT Regex & Namespaces");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        String configFile = cmd.getOptionValue("c");

        Yaml yaml = new Yaml(new Constructor(Config.class));
        InputStream is = new FileInputStream(configFile);
        config = yaml.load(is);

        long start = System.currentTimeMillis();
        long end;
        if (config.logType.equals("unix")) {
            log.info("start unix log parser");

            Parser unixParser = new UnixParser(config);
            unixParser.generateOttrMap();
            unixParser.parseLogpaiData();

            log.info("*** unix log processing finished ***");
        } else {
            log.error("*** Unsupported log type ***");
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
