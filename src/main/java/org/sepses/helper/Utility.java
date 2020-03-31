package org.sepses.helper;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.sepses.yaml.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Utility {

    public static final String TIME_ZONE = "Europe/Vienna";
    public static final String SECONDS = "SECONDS";
    public static final String OTTR_IRI = "ottr:IRI";

    private static final Logger log = LoggerFactory.getLogger(Utility.class);

    /**
     * *** create hash out of content
     *
     * @param templateText
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String createHash(String templateText) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        final byte[] hashbytes = digest.digest(templateText.getBytes(StandardCharsets.UTF_8));
        return DigestUtils.sha256Hex(hashbytes);
    }

    public static String cleanContent(String inputContent) {

        String cleanContent = inputContent.replace("\\", "|"); // clean up cleanContent
        cleanContent = cleanContent.replaceAll("\"", "|");
        cleanContent = cleanContent.replaceAll("'", "|");

        return cleanContent;
    }

    public static String cleanUriContent(String inputContent) {

        String cleanContent = inputContent.replaceAll("[^a-zA-Z0-9._-]", "_");
        cleanContent = cleanContent.replaceAll("\\.+$", "");

        return cleanContent;
    }

    public static void writeToFile(String string, String filename) throws IOException {
        FileWriter writer = new FileWriter(filename);
        writer.write(string);
        writer.flush();
        writer.close();
    }

    public static Map<String, Template> loadTemplates(Iterable<CSVRecord> existingTemplates, Config config) {

        Map<String, Template> templatesMap = new HashMap<>();
        existingTemplates.forEach(existingTemplate -> {
            Template template = Template.parseExistingTemplate(existingTemplate, config);
            templatesMap.put(template.hash, template);
        });

        return templatesMap;
    }

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

        SimpleDateFormat xmlDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
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
                fromFormatter = DateTimeFormatter.ofPattern(timeFormat);
                dateTime = LocalDateTime.parse(timeParam, fromFormatter);
            } catch (IllegalArgumentException e) {
                log.error(e.getMessage());
            } catch (DateTimeParseException e) {
                log.error(e.getMessage());
            }
        }

        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }
}
