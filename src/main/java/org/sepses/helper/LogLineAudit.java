package org.sepses.helper;

import org.apache.commons.csv.CSVRecord;
import org.sepses.yaml.InternalLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;

public class LogLineAudit extends LogLine {
    private static final Logger log = LoggerFactory.getLogger(LogLineUnix.class);
    private static final String[] TEMPLATE_HEADER =
            { "LineId", "type", "time", "Content", "EventId", "EventTemplate", "ParameterList" };

    public LogLineAudit(CSVRecord record, InternalLogType ilogType) throws NoSuchAlgorithmException {
        super(record, ilogType);

        counter = Integer.parseInt(record.get(TEMPLATE_HEADER[0]));
        dateTime = getDate(record.get(TEMPLATE_HEADER[2]));
        content = record.get(TEMPLATE_HEADER[3]);
        logpaiEventId = record.get(TEMPLATE_HEADER[4]);
        templateHash = Utility.createHash(record.get(TEMPLATE_HEADER[5]));
        setParameters(record.get(TEMPLATE_HEADER[6]));
    }

    private static String getDate(String timeSinceEpoch) {
        String time = timeSinceEpoch.split(":")[0].replaceAll("\\.", "");
        time = time.substring(0, time.length()-3); // convert from ms to second
        return Utility.localTimeConversion(time, Utility.SECONDS);
    }
}
