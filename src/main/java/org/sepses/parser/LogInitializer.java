package org.sepses.parser;

import org.apache.commons.io.FileUtils;
import org.sepses.slogert.config.ExtractionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class LogInitializer {

    private static final Logger log = LoggerFactory.getLogger(LogInitializer.class);
    private static final String PYTHON_SCRIPT = "logpai/scenario.py";
    private static final String PYTHON_BASE_SCRIPT = "logpai/scenario-base.py";

    public static void initialize(ExtractionConfig config) throws IOException {

        log.info("Start conversion from OTTR instances into Turtle file");

        initializeLogFiles(new File(config.rawFolder), config.initializedFolder);

        File outputError = new File("error.log");

        Path path_base = Paths.get(PYTHON_BASE_SCRIPT);
        Path path = Paths.get(PYTHON_SCRIPT);
        Charset charset = StandardCharsets.UTF_8;
        String content = new String(Files.readAllBytes(path_base), charset);
        content = content.replaceAll("\\$input_dir\\$", config.initializedFolder);
        content = content.replaceAll("\\$output_dir\\$", config.preprocessedFolder);
        content = content.replaceAll("\\$log_file\\$", config.source);
        content = content.replaceAll("\\$log_format\\$", config.format);

        Files.write(path, content.getBytes(charset));

        ProcessBuilder pb = new ProcessBuilder("./" + PYTHON_SCRIPT);
        pb.redirectError(ProcessBuilder.Redirect.to(outputError));
        Process p = pb.start();

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("done");
    }

    public static void initializeLogFiles(File inputFile, String outputFolder) throws IOException {
        FileUtils.deleteDirectory(new File(outputFolder));
        List<File> files = Arrays.asList(inputFile.listFiles());

        for (File file : files) {

            String device = file.getName();
            log.info("Start" + file.getPath());

            List<File> logFiles = Arrays.asList(file.listFiles());
            for (File logFile : logFiles) {

                if (logFile.getName().contains("DS_STORE"))
                    continue;

                Path outputPath = createOrRetrieve(outputFolder, logFile.getName()).toPath();

                if (Files.notExists(outputPath))
                    Files.createFile(outputPath);

                try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
                    String line;
                    StringBuilder sb = new StringBuilder();
                    int counter = 0;
                    int iteration = 1;
                    while ((line = br.readLine()) != null) {
                        counter++;
                        sb.append(device).append(" ").append(line).append(System.lineSeparator());
                        if (counter > 100000) {
                            // write to file
                            Files.write(outputPath, sb.toString().getBytes(), StandardOpenOption.APPEND);
                            sb = new StringBuilder();
                            log.info("File: '" + logFile.getName() + "' iteration-" + iteration);

                            counter = 0;
                            iteration++;
                        }
                    }
                    Files.write(outputPath, sb.toString().getBytes(), StandardOpenOption.APPEND);
                }
            }
            log.info("End" + file.getPath());
        }
    }

    /**
     * Creates parent directories if necessary. Then returns file
     */
    private static File createOrRetrieve(String directory, String filename) {
        File dir = new File(directory);
        if (!dir.exists())
            dir.mkdirs();
        return new File(directory + "/" + filename);
    }
}
