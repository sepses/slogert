package org.sepses.logline;

import org.apache.commons.csv.CSVRecord;
import org.sepses.helper.Utility;
import org.sepses.yaml.InternalLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;

public class LogLineApache extends LogLine {
    private static final Logger log = LoggerFactory.getLogger(LogLineUnix.class);
    private static final String[] TEMPLATE_HEADER =
            { "Device", "LineId", "IP", "DateTime", "TimeZone", "Content", "EventId", "EventTemplate",
                    "ParameterList" };

    public LogLineApache(CSVRecord record, InternalLogType ilogType) throws NoSuchAlgorithmException {
        super(record, ilogType);

        device = record.get(TEMPLATE_HEADER[0]);
        counter = Integer.parseInt(record.get(TEMPLATE_HEADER[1]));
        specialParameters.put("ip", TEMPLATE_HEADER[2]); // TODO - Handle this!!!
        dateTime = getDate(record.get(TEMPLATE_HEADER[3]), record.get(TEMPLATE_HEADER[4]));
        content = record.get(TEMPLATE_HEADER[5]);
        logpaiEventId = record.get(TEMPLATE_HEADER[6]);
        templateHash = Utility.createHash(record.get(TEMPLATE_HEADER[7]));
        setParameters(record.get(TEMPLATE_HEADER[8]));
    }

    private static String getDate(String dateTime, String timeZone) {
        String timeString = dateTime + timeZone;
        return Utility.localTimeConversion(timeString, "dd/MMM/yyyy':'HH:mm:ssZ");
    }
}
