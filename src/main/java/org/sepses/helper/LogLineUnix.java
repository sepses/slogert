package org.sepses.helper;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LogLineUnix extends LogLine {

    private static final Logger log = LoggerFactory.getLogger(LogLineUnix.class);
    private static final String[] TEMPLATE_UNIX_LOG =
            { "LineId", "Month", "Date", "Time", "Type", "Component", "Content", "EventId", "EventTemplate",
                    "ParameterList" };

    private LogLineUnix(CSVRecord record) throws NoSuchAlgorithmException {
        super();

        counter = Integer.parseInt(record.get(TEMPLATE_UNIX_LOG[0]));
        dateTime = getDate(record.get(TEMPLATE_UNIX_LOG[1]), record.get(TEMPLATE_UNIX_LOG[2]),
                record.get(TEMPLATE_UNIX_LOG[3]));
        content = record.get(TEMPLATE_UNIX_LOG[6]);
        logpaiEventId = record.get(TEMPLATE_UNIX_LOG[7]);
        templateHash = Utility.createHash(record.get(TEMPLATE_UNIX_LOG[8]));

        setParameters(record.get(TEMPLATE_UNIX_LOG[9]));

    }

    public static LogLine getInstance(CSVRecord record) {
        LogLine logLine;
        try {
            logLine = new LogLineUnix(record);
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
            logLine = null;
        }
        return logLine;
    }

    private String getDate(String month, String day, String time) {

        day = StringUtils.leftPad(day, 2, "0");
        LocalTime localTime = LocalTime.parse(time);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, DateTime.now().getYear());
        cal.set(Calendar.MONTH, 1); // default
        try {
            cal.set(Calendar.MONTH, new SimpleDateFormat("MMM", Locale.ENGLISH).parse(month).getMonth());
        } catch (ParseException e) {
            log.error("incorrect month format - defaulted to January");
            log.error(e.getMessage());
        }
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
        cal.set(Calendar.HOUR, localTime.getHour());
        cal.set(Calendar.MINUTE, localTime.getMinute());
        cal.set(Calendar.SECOND, localTime.getSecond());

        Date dateRepresentation = cal.getTime();

        SimpleDateFormat xmlDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String dateString;

        dateString = xmlDateFormatter.format(dateRepresentation);

        return dateString;
    }

}
