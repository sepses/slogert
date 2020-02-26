package org.sepses;

import org.apache.commons.cli.*;
import org.sepses.helper.Utility;
import org.sepses.processor.Parser;
import org.sepses.processor.UnixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.sepses.helper.Utility.writeToFile;
import static org.sepses.processor.UnixParser.BASE_OTTR_RULE;

public class MainParser {

    private static final Logger log = LoggerFactory.getLogger(MainParser.class);

    public static void main(String[] args) throws ParseException, IOException {

        Options options = new Options();
        options.addRequiredOption("t", "template-file", true, "Logpai log template file location");
        options.addRequiredOption("l", "log-data", true, "Logpai structured log file location");
        options.addRequiredOption("o", "log-type", true, "Type of log (e.g., unix, apache)");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        String template = cmd.getOptionValue("t");
        String logFile = cmd.getOptionValue("l");
        String logType = cmd.getOptionValue("o");

        long start = System.currentTimeMillis();
        long end;
        if (logType.equals("unix")) {
            log.info("start unix log parser");
            Parser unixParser = new UnixParser(template, logFile, true);
            String authMapping = Utility.generateOttrMap(unixParser.getHashTemplates(), BASE_OTTR_RULE);
            String authData = unixParser.parseLogpaiData(logFile);
            writeToFile(authMapping, template + ".ottr");
            writeToFile(authData, logFile + ".ottr");
            log.info("*** unix log processing finished ***");
        } else {
            log.error("*** Unsupported log type ***");
        }

        end = System.currentTimeMillis();
        log.info("Transformation process finished in " + (end - start) + " milliseconds");
        System.gc();

    }

}
