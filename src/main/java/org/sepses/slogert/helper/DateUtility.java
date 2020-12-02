package org.sepses.slogert.helper;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtility {

    private static final Logger log = LoggerFactory.getLogger(DateUtility.class);

    public static final String TIME_ZONE = "Europe/Vienna";
    public static final String SECONDS = "SECONDS";
    public static final String XSD_DATETIME = "yyyy-MM-dd'T'HH:mm:ss";

    public static String getDate(String dateTime, String timeZone) {
        String timeString = dateTime + timeZone;
        return localTimeConversion(timeString, "dd/MMM/yyyy':'HH:mm:ssZ");
    }

    /**
     * Date string generation
     *
     * @param month
     * @param day
     * @param time
     * @return String date
     */
    public static String getDate(String month, String day, String time) {

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

        SimpleDateFormat xmlDateFormatter = new SimpleDateFormat(XSD_DATETIME);
        String dateString;

        dateString = xmlDateFormatter.format(dateRepresentation);

        return dateString;
    }

    public static String getDate(String timeSinceEpoch) {
        String time = timeSinceEpoch.split(":")[0].replaceAll("\\.", "");
        time = time.substring(0, time.length() - 3); // convert from ms to second
        return localTimeConversion(time, SECONDS);
    }

    public static String getDate(String month, String day, String time, String year) {

        day = StringUtils.leftPad(day, 2, "0");
        LocalTime localTime = LocalTime.parse(time);

        Calendar cal = Calendar.getInstance();
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
        cal.set(Calendar.YEAR, Integer.parseInt(year));

        Date dateRepresentation = cal.getTime();

        SimpleDateFormat xmlDateFormatter = new SimpleDateFormat(XSD_DATETIME);
        String dateString;

        dateString = xmlDateFormatter.format(dateRepresentation);

        return dateString;
    }

    public static String localTimeConversion(String timeParam, String timeFormat) {

        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        ZoneId zoneId = ZoneId.of(TIME_ZONE);
        ZoneOffset zoneOffSet = zoneId.getRules().getOffset(LocalDateTime.now());

        DateTimeFormatter fromFormatter;
        if (timeFormat.equalsIgnoreCase(SECONDS)) {
            dateTime = LocalDateTime.ofEpochSecond(Integer.parseInt(timeParam), 0, zoneOffSet);

        } else {
            try {
                fromFormatter = DateTimeFormatter.ofPattern(timeFormat, Locale.ENGLISH);
                dateTime = LocalDateTime.parse(timeParam, fromFormatter);
            } catch (IllegalArgumentException e) {
                log.error(e.getMessage());
            } catch (DateTimeParseException e) {
                log.error(e.getMessage());
            }
        }

        return dateTime.format(DateTimeFormatter.ofPattern(XSD_DATETIME));
    }
}
