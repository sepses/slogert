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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogInitializer {

    private static final Logger log = LoggerFactory.getLogger(LogInitializer.class);
    private static final String PYTHON_SCRIPT = "executable/logpai/scenario.py";
    private static final String PYTHON_BASE_SCRIPT = "executable/logpai/scenario-base.py";

    public static void initialize(ExtractionConfig config) throws IOException {

        log.info("Start conversion from OTTR instances into Turtle file");
        initLogFiles(config);

        int counter = 0;
        while (counter != -1) {
            String inputFile = config.source + "." + counter;
            Path path = Paths.get(config.initializedFolder, inputFile);
            if (Files.exists(path)) {
                runLogpai(config, inputFile);
                counter++;
            } else {
                counter = -1; // finish
            }
        }

        log.info("done");
    }

    public static void runLogpai(ExtractionConfig config, String inputFile) throws IOException {

        File outputError = new File("error.log");
        log.info("LogPai process started for file: '" + inputFile + "' started");

        Path path_base = Paths.get(PYTHON_BASE_SCRIPT);
        Path path = Paths.get(PYTHON_SCRIPT);
        Charset charset = StandardCharsets.UTF_8;
        String content = new String(Files.readAllBytes(path_base), charset);

        content = content.replaceAll("\\$input_dir\\$", config.initializedFolder);
        content = content.replaceAll("\\$output_dir\\$", config.preprocessedFolder);
        content = content.replaceAll("\\$log_file\\$", inputFile);
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
    }

    public static void initLogFiles(ExtractionConfig config) throws IOException {

        FileUtils.deleteDirectory(new File(config.initializedFolder));
        List<File> files = Arrays.asList(new File(config.rawFolder).listFiles());

        int iteration = 0;
        int counter = 0;
        StringBuilder sb = new StringBuilder();

        Path outputPath = createOrRetrieve(config.initializedFolder, config.source + "." + iteration).toPath();
        if (Files.notExists(outputPath))
            Files.createFile(outputPath);

        for (File file : files) {

            String device = file.getName(); // device name - TODO: make it more general
            List<String> paths = getPaths(file, config.source); // get all path to the selected source files

            for (String pathToFile : paths) {

                try (BufferedReader br = new BufferedReader(new FileReader(pathToFile))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        counter++;
                        sb.append(device).append(" ").append(line).append(System.lineSeparator());
                        if (counter > config.logEventsPerExtraction) {
                            // write to file
                            Files.write(outputPath, sb.toString().getBytes(), StandardOpenOption.CREATE);
                            sb = new StringBuilder();
                            log.info("File: '" + outputPath + "' iteration-" + iteration);

                            counter = 0;
                            iteration++;
                            outputPath = createOrRetrieve(config.initializedFolder, config.source + "." + iteration)
                                    .toPath();
                            if (Files.notExists(outputPath))
                                Files.createFile(outputPath);
                        }
                    }
                }
            }
        }

        Files.write(outputPath, sb.toString().getBytes(), StandardOpenOption.APPEND);
        log.info("File: '" + outputPath + "' iteration-" + iteration);

    }

    public static List<String> getPaths(File inputFolder, String inputFile) {
        List<String> paths = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(inputFolder.toPath())) {
            List<String> result =
                    walk.filter(Files::isRegularFile).filter(file -> file.toString().endsWith(inputFile)).map(x -> x.toString())
                            .collect(Collectors.toList());
            paths.addAll(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return paths;
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
