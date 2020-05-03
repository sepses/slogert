package org.sepses.logline;

import org.apache.commons.csv.CSVRecord;
import org.sepses.helper.Utility;
import org.sepses.yaml.InternalLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;

public class LogLineFTP extends LogLine {

    private static final Logger log = LoggerFactory.getLogger(LogLineFTP.class);
    private static final String[] TEMPLATE_HEADER =
            { "Device", "LineId", "dayOfWeek", "month", "day", "time", "year", "pid", "pidNumber", "Content", "EventId",
                    "EventTemplate", "ParameterList" };

    public LogLineFTP(CSVRecord record, InternalLogType ilogType) throws NoSuchAlgorithmException {
        super(record, ilogType);

        device = record.get(TEMPLATE_HEADER[0]);
        counter = Integer.parseInt(record.get(TEMPLATE_HEADER[1]));
        dateTime = Utility.getDate(record.get(TEMPLATE_HEADER[3]), record.get(TEMPLATE_HEADER[4]),
                record.get(TEMPLATE_HEADER[5]));
        content = record.get(TEMPLATE_HEADER[9]);
        logpaiEventId = record.get(TEMPLATE_HEADER[10]);
        templateHash = Utility.createHash(record.get(TEMPLATE_HEADER[11]));

        setParameters(record.get(TEMPLATE_HEADER[12]));

    }

}
