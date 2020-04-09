package org.sepses.helper;

import org.apache.commons.csv.CSVRecord;
import org.sepses.yaml.InternalLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;

public class LogLineApache extends LogLine {
    private static final Logger log = LoggerFactory.getLogger(LogLineUnix.class);
    private static final String[] TEMPLATE_HEADER =
            //            { "LineId", "type", "time", "Content", "EventId", "EventTemplate", "ParameterList" };
            { "LineId", "IP", "DateTime", "TimeZone", "Content", "EventId", "EventTemplate", "ParameterList" };

    public LogLineApache(CSVRecord record, InternalLogType ilogType) throws NoSuchAlgorithmException {
        super(record, ilogType);

        counter = Integer.parseInt(record.get(TEMPLATE_HEADER[0]));
        specialParameters.put("ip", TEMPLATE_HEADER[1]); // TODO - Handle this!!!
        dateTime = getDate(record.get(TEMPLATE_HEADER[2]), record.get(TEMPLATE_HEADER[3]));
        content = record.get(TEMPLATE_HEADER[4]);
        logpaiEventId = record.get(TEMPLATE_HEADER[5]);
        templateHash = Utility.createHash(record.get(TEMPLATE_HEADER[6]));
        setParameters(record.get(TEMPLATE_HEADER[7]));
    }

    private static String getDate(String dateTime, String timeZone) {
        String timeString = dateTime + timeZone;
        return Utility.localTimeConversion(timeString, "dd/MMM/yyyy':'HH:mm:ssZ");
    }
}
