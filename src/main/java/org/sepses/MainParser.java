package org.sepses;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import org.apache.commons.cli.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.sepses.config.ExtractionConfig;
import org.sepses.config.Parameter;
import org.sepses.event.LogEvent;
import org.sepses.event.LogEventTemplate;
import org.sepses.helper.Utility;
import org.sepses.nlp.EntityRecognition;
import org.sepses.ottr.OttrInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.sepses.helper.Utility.getLogEventTemplateMap;

public class MainParser {

    private static final Logger log = LoggerFactory.getLogger(MainParser.class);
    private static final String CONFIG_YAML = "config.yaml";
    private static ExtractionConfig config;

    public static void main(String[] args) throws ParseException, IOException {

        Options options = new Options();
        options.addRequiredOption("c", "config-io", true, "SLOGERT Configuration for I/O");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        String ioString = cmd.getOptionValue("c").trim();
        File ioFile = new File(ioString);
        if (!ioFile.isFile())
            return;
        log.info("*** YAML input config-io file is valid");

        // ** config initialization
        Yaml yaml = new Yaml(new Constructor(ExtractionConfig.class));
        InputStream configIS = MainParser.class.getClassLoader().getResourceAsStream(CONFIG_YAML);
        InputStream ioIS = new FileInputStream(ioFile);
        InputStream is = new SequenceInputStream(configIS, ioIS);
        config = yaml.load(is);
        extractExtraConfig(config); // done
        log.info("*** YAML files configuration is being loaded");

        try {
            StringBuilder timerSB = new StringBuilder();

            StringBuilder logSB = new StringBuilder();
            logSB.append(System.lineSeparator());
            logSB.append("****** Time log for file: " + config.logSourceType).append(System.lineSeparator());
            timerSB.append(Instant.now()).append(";");
            timerSB.append(config.source).append(";");

            Stopwatch timer = Stopwatch.createStarted();
            extractNerRules(config); // done
            logSB.append("*** Standford NER rules are generated in " + timer.stop()).append(System.lineSeparator());
            timerSB.append(timer.elapsed(TimeUnit.MILLISECONDS)).append(";");

            timer = Stopwatch.createStarted();
            extractLogEventTemplates(config); // done
            logSB.append("*** LogEvent templates are generated in " + timer.stop()).append(System.lineSeparator());
            timerSB.append(timer.elapsed(TimeUnit.MILLISECONDS)).append(";");

            timer = Stopwatch.createStarted();
            extractOttrBase(config); // done
            logSB.append("*** OTTR templates are generated in " + timer.stop()).append(System.lineSeparator());
            timerSB.append(timer.elapsed(TimeUnit.MILLISECONDS)).append(";");

            timer = Stopwatch.createStarted();
            extractLogEvents(config); // done
            logSB.append("*** OTTR instances are generated in " + timer.stop()).append(System.lineSeparator());
            timerSB.append(timer.elapsed(TimeUnit.MILLISECONDS)).append("\n");

            try {
                Files.write(Paths.get(config.targetConfigTimer), timerSB.toString().getBytes(),
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
                log.error(e.getMessage());
            }

            logSB.append("****** End of log processing \n\n");
            logSB.append("Lutra execution command: ");
            logSB.append("time java -jar exe/lutra.jar --library ").append(config.targetOttrBase)
                    .append(" --libraryFormat stottr --inputFormat stottr ").append(config.targetOttr)
                    .append(" --mode expand --fetchMissing > ").append(config.targetOttrTurtle);

            log.info(logSB.toString());

        } catch (NoSuchElementException e) {
            log.error("*** Unsupported log type ***");
            log.error(e.getMessage());

        }

        System.gc();

    }

    /**
     * derive additional configuration from initial config file.
     *
     * @param config
     */
    private static void extractExtraConfig(ExtractionConfig config) {

        config.logFormatInstance =
                config.logFormats.stream().filter(format -> config.logFormat.equals(format.id)).findFirst()
                        .orElse(null);
        config.logFormatOttrBase = config.ottrTemplates.stream()
                .filter(template -> config.logFormatInstance.ottrBaseTemplate.equals(template.uri)).findFirst()
                .orElse(null);
    }

    /**
     * Extracting OTTR template for the running conversion process
     *
     * @param config
     * @throws IOException
     */
    private static void extractOttrBase(ExtractionConfig config) throws IOException {

        StringBuilder sb = new StringBuilder();

        config.namespaces.forEach(ns -> {
            sb.append("@prefix ").append(ns.prefix).append(": <").append(ns.uri).append("> . \n");
        });

        sb.append("\n### Basic OTTR templates \n\n");
        config.ottrTemplates.forEach(ot -> {
            sb.append(Utility.buildOttrString(ot));
        });

        sb.append("\n### Parameter OTTR templates \n\n");
        Stream<Parameter> ps = Stream.concat(config.nerParameters.stream(), config.nonNerParameters.stream());
        ps.forEach(parameter -> {
            sb.append(Utility.buildOttrString(parameter.ottrTemplate));
        });

        sb.append("\n### LogEventTemplate OTTR templates \n\n");
        config.logEventTemplates.values().stream().forEach(let -> {
            sb.append(Utility.buildOttrString(let.generateOttrTemplate(config)));
        });

        Utility.writeToFile(sb.toString(), config.targetOttrBase);
    }

    /**
     * Generation of OTTR instances from log events
     *
     * @param config
     * @param events
     * @throws IOException
     */
    private static void extractOttrInstances(ExtractionConfig config, List<LogEvent> events) throws IOException {
        Integer maxCount = config.logEventsPerExtraction;

        if (events.size() > maxCount) {
            String filename = config.targetOttr;
            String path = FilenameUtils.getPath(filename);
            String base = FilenameUtils.getBaseName(filename);
            String extension = FilenameUtils.getExtension(filename);

            List<List<LogEvent>> partitions = Lists.partition(events, maxCount);
            for (int i = 0; i < partitions.size(); i++) {
                filename = new StringBuilder(path).append(base).append("-").append(i).append(".").append(extension)
                        .toString();
                executeExtract(config, partitions.get(i), filename);
            }

        } else {
            executeExtract(config, events, config.targetOttr);
        }
    }

    private static void executeExtract(ExtractionConfig config, List<LogEvent> events, String fileName)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        config.namespaces.forEach(ns -> {
            sb.append("@prefix ").append(ns.prefix).append(": <").append(ns.uri).append("> . \n");
        });

        // add metadata on log sources
        sb.append("\n### ottr metadata \n\n");
        OttrInstance ottr = Utility.createOttrMetadata(config);
        sb.append(ottr.toString());

        // add instances
        sb.append("\n### ottr instances \n\n");
        events.forEach(event -> {
            OttrInstance ot = event.toOttrInstance(config);
            sb.append(ot.toString());
        });
        Utility.writeToFile(sb.toString(), fileName);
    }

    /**
     * Generatiion of @{@link LogEvent} from the input CSV instances file
     *
     * @param config
     * @throws IOException
     */
    private static void extractLogEvents(ExtractionConfig config) throws IOException {

        // *** read and collect input logpai data
        log.info("Start reading logpai data for LogEventTemplate parameter information extractions");
        Reader dataReader = new FileReader(Paths.get(config.sourceLogpai).toFile());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(dataReader);
        List<LogEvent> logEvents = new ArrayList<>();
        records.forEach(record -> logEvents.add(new LogEvent(record, config)));

        extractOttrInstances(config, logEvents);

    }

    /**
     * Generation of @{@link LogEventTemplate} from the input CSV template file
     *
     * @param config
     */
    private static void extractLogEventTemplates(ExtractionConfig config) {

        log.debug("read and collect input logpai structure");
        try {

            // *** read and collect input logpai structure
            Reader templateReader = new FileReader(Paths.get(config.sourceLogpaiTemplate).toFile());
            Iterable<CSVRecord> inputTemplates = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(templateReader);
            log.debug("read and collect input logpai structure");

            // *** if desired, we can load existing template ***
            if (!config.isOverrideExisting) {
                // *** load existing hashTemplates
                config.logEventTemplates.putAll(getLogEventTemplateMap(config));
            }

            // *** Create LogEventTemplate ***

            log.info("LogEventTemplate creation started");
            // * initiate templates with keywords and log source type
            for (CSVRecord templateCandidate : inputTemplates) {
                String eventTemplate = templateCandidate.get(LogEvent.LOGPAI_EVENT_TEMPLATE);
                String hashCandidate = Utility.createHash(eventTemplate);

                LogEventTemplate let;
                if (!config.logEventTemplates.containsKey(hashCandidate)) {
                    let = new LogEventTemplate();
                    EntityRecognition er =
                            EntityRecognition.getInstanceConfig(config.targetStanfordNer, config.nonNerParameters);

                    let.label = hashCandidate;
                    let.keywords = er.extractKeywords(eventTemplate);
                    let.pattern = templateCandidate.get(LogEvent.LOGPAI_EVENT_TEMPLATE);

                    config.logEventTemplates.put(hashCandidate, let);
                } else {
                    let = config.logEventTemplates.get(hashCandidate);
                }
                let.logSourceTypes.add(config.logSourceType);

            }
            log.info("LogEventTemplates creation finished (no parameter information)");

            // *** Add LogEventTemplate parameters information to LogEventTemplate

            // * read and collect input logpai data
            log.info("Start reading logpai data for LogEventTemplate parameter information extractions");
            Reader dataReader = new FileReader(Paths.get(config.sourceLogpai).toFile());
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(dataReader);
            List<LogEvent> logEvents = new ArrayList<>();
            records.forEach(record -> logEvents.add(new LogEvent(record, config)));

            // * only process logpai data when the LogEventTemplate is empty
            log.info("Start logEvent reading");

            logEvents.stream().forEach(logEvent -> {
                LogEventTemplate let = config.logEventTemplates.get(logEvent.templateHash);

                if (let.parameters.isEmpty()) {
                    let.example = logEvent.content;
                    let.extractionCount = 1;
                    // * set & process nerParameters
                    logEvent.contentParameters.forEach(param -> {
                        String paramType = EntityRecognition.getParamType(logEvent, param, config);
                        let.parameters.add(paramType);
                    });
                } else {
                    // * if it's unknown, probably we retry it with next lines - up to "paramExtractAttempt" attempts
                    if (let.extractionCount++ <= config.paramExtractAttempt) {
                        for (int i = 0; i < let.parameters.size(); i++) {
                            String templateParam = let.parameters.get(i);
                            if (templateParam.equals(LogEvent.UNKNOWN_PARAMETER)) {
                                String param = logEvent.contentParameters.get(i);
                                let.parameters.set(i, EntityRecognition.getParamType(logEvent, param, config));
                            }
                        }
                    }
                }
            });
            log.debug("End of LogEventTemplate parameter information extractions");

            // *** write templates
            Model model = Utility.createModel(config);
            config.logEventTemplates.values().stream().forEach(item -> model.add(item.toModel()));
            log.info("LogEventTemplates are committed");

            FileOutputStream fos = new FileOutputStream(config.targetConfigTurtle);
            RDFDataMgr.write(fos, model, RDFFormat.TRIG);
            fos.close();
            model.close();
            log.info("LogEventTemplates file is written");

        } catch (Exception e) {
            log.error("*** Error ***");
            log.error(e.getMessage());
        }
    }

    /**
     * Extraction of NER rules from the configuration file.
     *
     * @param config
     * @throws IOException
     */
    private static void extractNerRules(ExtractionConfig config) throws IOException {

        StringBuilder sb = new StringBuilder();
        InputStream nerIS = MainParser.class.getClassLoader().getResourceAsStream("config-base.rules");
        sb.append(IOUtils.toString(nerIS, Charset.defaultCharset()));

        config.nerParameters.forEach(parameter -> {
            sb.append("{ ruleType: \"tokens\", ");
            sb.append("pattern: ").append(parameter.pattern).append(", ");
            sb.append("action: ").append(parameter.action).append(", ");
            sb.append("result: \"").append(parameter.id).append("\" }");
            sb.append(System.lineSeparator());
        });

        Utility.writeToFile(sb.toString(), config.targetStanfordNer);

    }

}
