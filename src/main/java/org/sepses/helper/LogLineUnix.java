package org.sepses.helper;

import org.apache.commons.csv.CSVRecord;
import org.sepses.yaml.InternalLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;

public class LogLineUnix extends LogLine {

    private static final Logger log = LoggerFactory.getLogger(LogLineUnix.class);
    private static final String[] TEMPLATE_UNIX_LOG =
            { "LineId", "Month", "Date", "Time", "Type", "Component", "Content", "EventId", "EventTemplate",
                    "ParameterList" };

    public LogLineUnix(CSVRecord record, InternalLogType ilogType) throws NoSuchAlgorithmException {
        super(record, ilogType);

        counter = Integer.parseInt(record.get(TEMPLATE_UNIX_LOG[0]));
        dateTime = Utility.getDate(record.get(TEMPLATE_UNIX_LOG[1]), record.get(TEMPLATE_UNIX_LOG[2]),
                record.get(TEMPLATE_UNIX_LOG[3]));
        content = record.get(TEMPLATE_UNIX_LOG[6]);
        logpaiEventId = record.get(TEMPLATE_UNIX_LOG[7]);
        templateHash = Utility.createHash(record.get(TEMPLATE_UNIX_LOG[8]));

        setParameters(record.get(TEMPLATE_UNIX_LOG[9]));

    }

}
