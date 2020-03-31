package org.sepses.helper;

import org.apache.commons.csv.CSVRecord;
import org.sepses.yaml.InternalLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;

public class LogLineFTP extends LogLine {

    private static final Logger log = LoggerFactory.getLogger(LogLineFTP.class);
    private static final String[] TEMPLATE_UNIX_LOG =
            //            {"LineId", "Month", "Date", "Time", "Type", "Component", "Content", "EventId", "EventTemplate","ParameterList" };
            { "LineId", "dayOfWeek", "month", "day", "time", "year", "pid", "pidNumber", "Content", "EventId",
                    "EventTemplate", "ParameterList" };

    public LogLineFTP(CSVRecord record, InternalLogType ilogType) throws NoSuchAlgorithmException {
        super(record, ilogType);

        counter = Integer.parseInt(record.get(TEMPLATE_UNIX_LOG[0]));
        dateTime = Utility.getDate(record.get(TEMPLATE_UNIX_LOG[2]), record.get(TEMPLATE_UNIX_LOG[3]),
                record.get(TEMPLATE_UNIX_LOG[4]));
        content = record.get(TEMPLATE_UNIX_LOG[8]);
        logpaiEventId = record.get(TEMPLATE_UNIX_LOG[9]);
        templateHash = Utility.createHash(record.get(TEMPLATE_UNIX_LOG[10]));

        setParameters(record.get(TEMPLATE_UNIX_LOG[11]));

    }

}
