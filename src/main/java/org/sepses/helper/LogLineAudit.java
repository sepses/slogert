package org.sepses.helper;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Instant;

public class LogLineAudit extends LogLine {

    private static final Logger log = LoggerFactory.getLogger(LogLineUnix.class);
    private static final String[] TEMPLATE_UNIX_LOG =
            { "LineId", "type", "time", "Content", "EventId", "EventTemplate", "ParameterList" };

    private LogLineAudit(CSVRecord record) throws NoSuchAlgorithmException {
        super();

        counter = Integer.parseInt(record.get(TEMPLATE_UNIX_LOG[0]));
        specialParameters.put("auditType", TEMPLATE_UNIX_LOG[1]);
        dateTime = getDate(record.get(TEMPLATE_UNIX_LOG[2]));
        content = record.get(TEMPLATE_UNIX_LOG[3]);
        logpaiEventId = record.get(TEMPLATE_UNIX_LOG[4]);
        templateHash = Utility.createHash(record.get(TEMPLATE_UNIX_LOG[5]));
        setParameters(record.get(TEMPLATE_UNIX_LOG[6]));
    }

    public static LogLine getInstance(CSVRecord record) {
        LogLine logLine;
        try {
            logLine = new LogLineAudit(record);
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
            logLine = null;
        }
        return logLine;
    }

    private static String getDate(String timeSinceEpoch) {
        String time = timeSinceEpoch.split(":")[0].replaceAll("\\.", "");
        Long timeInMs = Long.parseLong(time);
        Instant instant = Instant.ofEpochMilli(timeInMs);
        String dateString = instant.toString();
        String date = dateString.substring(0, dateString.indexOf('.'));

        return date;
    }

    public static void main(String[] args) {
        System.out.println(LogLineAudit.getDate("1583678591.423:45"));

    }
}
