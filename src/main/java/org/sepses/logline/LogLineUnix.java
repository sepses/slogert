package org.sepses.logline;

import org.apache.commons.csv.CSVRecord;
import org.sepses.helper.Utility;
import org.sepses.yaml.InternalLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;

public class LogLineUnix extends LogLine {

    private static final Logger log = LoggerFactory.getLogger(LogLineUnix.class);
    private static final String[] TEMPLATE_HEADER =
            { "Device", "LineId", "Month", "Date", "Time", "Type", "Component", "Content", "EventId", "EventTemplate",
                    "ParameterList" };

    public LogLineUnix(CSVRecord record, InternalLogType ilogType) throws NoSuchAlgorithmException {
        super(record, ilogType);

        device = record.get(TEMPLATE_HEADER[0]);
        counter = Integer.parseInt(record.get(TEMPLATE_HEADER[1]));
        dateTime = Utility.getDate(record.get(TEMPLATE_HEADER[2]), record.get(TEMPLATE_HEADER[3]),
                record.get(TEMPLATE_HEADER[4]));
        content = record.get(TEMPLATE_HEADER[7]);
        logpaiEventId = record.get(TEMPLATE_HEADER[8]);
        templateHash = Utility.createHash(record.get(TEMPLATE_HEADER[9]));

        setParameters(record.get(TEMPLATE_HEADER[10]));

    }

}
