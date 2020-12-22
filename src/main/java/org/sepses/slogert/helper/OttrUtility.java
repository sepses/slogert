package org.sepses.slogert.helper;

import org.sepses.slogert.config.ExtractionConfig;
import org.sepses.slogert.ottr.OttrInstance;
import org.sepses.slogert.ottr.OttrTemplate;
import org.sepses.slogert.rdf.LOG;
import org.sepses.slogert.rdf.LOGEX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringJoiner;

public class OttrUtility {

    private static final Logger log = LoggerFactory.getLogger(OttrUtility.class);

    /**
     * Helper function to generate OTTR string for OTTR templates.
     *
     * @param ottrTemplate
     * @return
     */
    public static String buildOttrString(OttrTemplate ottrTemplate) {
        StringBuilder sb = new StringBuilder();

        StringJoiner sj = new StringJoiner(", ", "[", "]");
        ottrTemplate.parameters.forEach(parameter -> sj.add(parameter));
        sb.append(ottrTemplate.uri).append(sj).append(" :: { \n");

        StringJoiner tripleSJ = new StringJoiner(", \n");
        ottrTemplate.functions.forEach(function -> tripleSJ.add("\t" + function));
        sb.append(tripleSJ);

        sb.append("\n} . \n\n");

        return sb.toString();
    }

    /**
     * Create a metadata instance by a given config file
     *
     * @param config
     * @return OttrInstance representing a configuration file
     */
    public static OttrInstance createOttrMetadata(ExtractionConfig config) {

        OttrInstance ottr = new OttrInstance();
        ottr.uri =
                JenaUtility.getPrefixedName(LOGEX.OttrTemplate, LOGEX.NS_INSTANCE_PREFIX, LOG.Source.getLocalName());
        ottr.parameters.add(JenaUtility
                .getPrefixedName(LOG.Source, LOG.NS_INSTANCE_PREFIX, StringUtility.cleanUriContent(config.source)));
        ottr.parameters.add("\"" + config.source + "\"");
        ottr.parameters.add(JenaUtility.getPrefixedName(LOG.SourceType, LOG.NS_INSTANCE_PREFIX,
                StringUtility.cleanUriContent(config.logSourceType)));
        ottr.parameters.add("\"" + config.logSourceType + "\"");
        ottr.parameters.add(JenaUtility.getPrefixedName(LOG.Format, LOG.NS_INSTANCE_PREFIX,
                StringUtility.cleanUriContent(config.logFormat)));
        ottr.parameters.add("\"" + config.logFormat + "\"");

        return ottr;
    }

    public static void runOttrEngine(ExtractionConfig config) throws IOException {

        log.info("Start conversion from OTTR instances into Turtle file");

        Path path = Paths.get(config.targetOttr);
        if(Files.exists(path)) {

            Path targetTurtle = Paths.get(config.targetOttrTurtle);
            if (targetTurtle != null && !Files.exists(targetTurtle)) {
                targetTurtle.toFile().getParentFile().mkdirs();
            }

            File outputFile = new File(config.targetOttrTurtle);
            File outputError = new File(config.targetOttrTurtle + ".log");

            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "executable/lutra.jar", "--library", config.targetOttrBase,
                    "--libraryFormat", "stottr", "--inputFormat", "stottr", config.targetOttr, "--mode", "expand",
                    "--fetchMissing");
            pb.redirectOutput(ProcessBuilder.Redirect.to(outputFile));
            pb.redirectError(ProcessBuilder.Redirect.to(outputError));
            Process p = pb.start();
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
