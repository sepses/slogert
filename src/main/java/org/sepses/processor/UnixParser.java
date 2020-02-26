package org.sepses.processor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.sepses.helper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.sepses.helper.Template.loadTemplates;

public class UnixParser implements Parser {

    public static final String BASE_OTTR_RULE = "UnixOttr.stottr";
    private static final Logger log = LoggerFactory.getLogger(UnixParser.class);
    private static String[] TEMPLATE_LOGPAI = { "EventId", "EventTemplate", "Occurrences" };
    private final Map<String, Template> hashTemplates;
    private final String BASE_CSV_TEMPLATE = "UnixLogpai.csv";
    private final String BASE_OTTR_ID = "sepses:LogLine_";
    private final String NS_OBJECT = "http://w3id.org/sepses/slogert/";

    public UnixParser() {
        hashTemplates = new HashMap<>();
    }

    public UnixParser(String logpaiStructure, String logpaiData, Boolean isOverride) throws IOException {
        hashTemplates = new HashMap<>();
        createOrUpdateTemplate(logpaiStructure, logpaiData, isOverride);
    }

    /**
     * *** extract additional hashTemplates from input log (data+hashTemplates) if possible
     */
    public void extractTemplate(Iterable<CSVRecord> logpaiStructure, List<LogLine> inputData) {

        // Annotate template parameters
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
                            Template template = new Template(logpaiTemplate, hashCandidate, logLine);
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

    @Override public void createOrUpdateTemplate(String logpaiStructure, String logpaiData, Boolean isOverride)
            throws IOException {

        // *** load existing hashTemplates
        if (!isOverride) {
            File logpaiTemplate = new File(getClass().getClassLoader().getResource(BASE_CSV_TEMPLATE).getFile());
            Reader reader = new FileReader(logpaiTemplate);
            Iterable<CSVRecord> readerIterator = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
            hashTemplates.putAll(loadTemplates(readerIterator));
            reader.close();
        }

        // *** read and collect input logpai structure
        Reader templateReader = new FileReader(Paths.get(logpaiStructure).toFile());
        Iterable<CSVRecord> inputTemplates = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(templateReader);
        // *** read and collect input logpai data
        Reader dataReader = new FileReader(Paths.get(logpaiData).toFile());
        Iterable<CSVRecord> inputData = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(dataReader);
        List<LogLine> logLines = new ArrayList<>();
        inputData.forEach(inputRow -> logLines.add(UnixLogLine.getInstance(inputRow)));

        // *** derive hashTemplates
        extractTemplate(inputTemplates, logLines);
        templateReader.close();
        dataReader.close();
    }

    @Override public String parseLogpaiData(String logpaiData) throws IOException {

        // *** read and collect input logpai data
        Reader dataReader = new FileReader(Paths.get(logpaiData).toFile());
        Iterable<CSVRecord> inputData = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(dataReader);
        List<LogLine> logLines = new ArrayList<>();
        inputData.forEach(inputRow -> logLines.add(UnixLogLine.getInstance(inputRow)));
        dataReader.close();

        StringBuilder sb = new StringBuilder();
        sb.append("@prefix instance: <http://w3id.org/sepses/id/> .").append(System.lineSeparator());
        sb.append("@prefix sepses: <http://sepses.com/ns#> .").append(System.lineSeparator());
        sb.append("@prefix slog: <http://w3id.org/sepses/slogert/> .").append(System.lineSeparator());
        sb.append(System.lineSeparator());

        logLines.forEach(logLine -> {
            Template template = hashTemplates.get(logLine.getTemplateHash());

            sb.append(template.ottrId);
            sb.append("(instance:Logline_").append(UUID.randomUUID()).append(",\"");
            sb.append(logLine.getDateTime()).append("\",\"");
            sb.append(Utility.cleanContent(logLine.getContent())).append("\",\"");
            sb.append(logLine.getTemplateHash()).append("\",\"");

            for (int i = 0; i < template.parameters.size(); i++) {
                String paramString = logLine.getParameters().get(i);
                ParameterType paramType = template.parameters.get(i);
                if (paramType.equals(ParameterType.Unknown)) {
                    sb.append(Utility.cleanContent(paramString)).append("\",\"");
                } else {
                    sb.delete(sb.length() - 1, sb.length());
                    paramString = "slog:" + Utility.cleanUriContent(paramString);
                    sb.append(paramString).append(",\"");
                }
            }

            sb.delete(sb.length() - 2, sb.length());
            sb.append(") .").append(System.lineSeparator());
        });

        return sb.toString();
    }

    @Override public Map<String, Template> getHashTemplates() {
        return hashTemplates;
    }
}
