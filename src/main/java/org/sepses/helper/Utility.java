package org.sepses.helper;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.joda.time.DateTime;
import org.sepses.config.ExtractionConfig;
import org.sepses.event.LogEventTemplate;
import org.sepses.ottr.OttrInstance;
import org.sepses.ottr.OttrTemplate;
import org.sepses.rdf.LOG;
import org.sepses.rdf.LOGEX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Utility {

    public static final String TIME_ZONE = "Europe/Vienna";
    public static final String SECONDS = "SECONDS";
    public static final String XSD_DATETIME = "yyyy-MM-dd'T'HH:mm:ss";

    private static final Logger log = LoggerFactory.getLogger(Utility.class);

    /**
     * Load existing @{@link LogEventTemplate} map from the config-turtle.ttl (if any)
     *
     * @param config
     * @return
     * @throws IOException
     */
    public static Map<String, LogEventTemplate> getLogEventTemplateMap(ExtractionConfig config) throws IOException {
        Map<String, LogEventTemplate> templates = new HashMap<>();

        // *** load existing hashTemplates
        Model model = Utility.createModel(config);
        File configTurtle = new File(config.targetConfigTurtle);
        if (configTurtle.isFile()) {
            FileInputStream fis = new FileInputStream(config.targetConfigTurtle);
            RDFDataMgr.read(model, fis, Lang.TRIG);
            fis.close();
            templates.putAll(LogEventTemplate.fromModel(model, config));
        }
        model.close();

        return templates;
    }

    /**
     * Create a Jena Model that already includes all necessary namespaces and prefixes.
     *
     * @param config
     * @return Jena {@link Model}
     */
    public static Model createModel(ExtractionConfig config) {
        Model model = ModelFactory.createDefaultModel();
        config.namespaces.forEach(ns -> model.setNsPrefix(ns.prefix, ns.uri));
        return model;
    }

    /**
     * Create a resource from inputs
     *
     * @param cls
     * @param ns
     * @param label
     * @return @{@link Resource}
     */
    public static Resource createResource(Resource cls, String ns, String label) {
        StringBuilder sb = new StringBuilder();
        sb.append(ns).append(cls.getLocalName()).append("_").append(cleanUriContent(label));
        return ResourceFactory.createResource(sb.toString());
    }

    /**
     * Helper function to create a prefixed resource string
     *
     * @param cls
     * @param prefix
     * @param label
     * @return String
     */
    public static String getPrefixedName(Resource cls, String prefix, String label) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(":").append(cls.getLocalName()).append("_").append(cleanUriContent(label));
        return sb.toString();
    }

    public static String getDate(String dateTime, String timeZone) {
        String timeString = dateTime + timeZone;
        return Utility.localTimeConversion(timeString, "dd/MMM/yyyy':'HH:mm:ssZ");
    }

    /**
     * *** create hash out of content
     *
     * @param templateText
     * @return
     */
    public static String createHash(String templateText) {
        String hash = null;
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hashBytes = digest.digest(templateText.getBytes(StandardCharsets.UTF_8));
            hash = DigestUtils.sha256Hex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
        }
        return hash;
    }

    public static String cleanContent(String inputContent) {
        return inputContent.replaceAll("\"", "'").replaceAll("\\\\", "\\\\\\\\");
    }

    /**
     * Create a default model that contains all necessary prefixes
     *
     * @param config Slogert config
     * @return {@link Model}
     */
    public static Model createModelWithNS(ExtractionConfig config) {
        Model model = ModelFactory.createDefaultModel();
        config.namespaces.forEach(ns -> {
            model.setNsPrefix(ns.prefix, ns.uri);
        });
        return model;
    }

    /**
     * Cleaning string to comply to URI specification
     *
     * @param inputContent
     * @return
     */
    public static String cleanUriContent(String inputContent) {

        String cleanContent = inputContent.replaceAll("[^a-zA-Z0-9._-]", "_");
        cleanContent = cleanContent.replaceAll("\\.+$", "");

        return cleanContent;
    }

    /**
     * Wrting file to certain location
     *
     * @param string
     * @param filename
     * @throws IOException
     */
    public static void writeToFile(String string, String filename) throws IOException {
        File file = new File(filename);

        File folder = file.getParentFile();
        if (folder != null && !folder.exists())
            folder.mkdirs();

        FileWriter writer = new FileWriter(file);
        writer.write(string);
        writer.flush();
        writer.close();
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
        return Utility.localTimeConversion(time, Utility.SECONDS);
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

    /**
     * Helper function to generate OTTR string for OTTR templates.
     *
     * @param ottrTemplate
     * @return
     */
    public static String buildOttrString(OttrTemplate ottrTemplate) {
        StringBuilder sb = new StringBuilder();

        StringJoiner sj = new StringJoiner(", ", "[", "]");
        ottrTemplate.parameters.forEach(parameter -> sj.add(parameter));
        sb.append(ottrTemplate.uri).append(sj).append(" :: { \n");

        StringJoiner tripleSJ = new StringJoiner(", \n");
        ottrTemplate.functions.forEach(function -> tripleSJ.add("\t" + function));
        sb.append(tripleSJ);

        sb.append("\n} . \n\n");

        return sb.toString();
    }

    public static String cleanParameter(String s) {
        return s.replaceAll("'", "").replaceAll("\"", "").replaceAll(",", "").replaceAll("\\\\", "\\\\\\\\");
    }

    /**
     * Create a metadata instance by a given config file
     *
     * @param config
     * @return OttrInstance representing a configuration file
     */
    public static OttrInstance createOttrMetadata(ExtractionConfig config) {

        OttrInstance ottr = new OttrInstance();
        ottr.uri = Utility.getPrefixedName(LOGEX.OttrTemplate, LOGEX.NS_INSTANCE_PREFIX, LOG.Source.getLocalName());
        ottr.parameters.add(Utility
                .getPrefixedName(LOG.Source, LOG.NS_INSTANCE_PREFIX, Utility.cleanUriContent(config.source)));
        ottr.parameters.add("\"" + config.source + "\"");
        ottr.parameters.add(Utility.getPrefixedName(LOG.SourceType, LOG.NS_INSTANCE_PREFIX,
                Utility.cleanUriContent(config.logSourceType)));
        ottr.parameters.add("\"" + config.logSourceType + "\"");
        ottr.parameters.add(Utility
                .getPrefixedName(LOG.Format, LOG.NS_INSTANCE_PREFIX, Utility.cleanUriContent(config.logFormat)));
        ottr.parameters.add("\"" + config.logFormat + "\"");

        return ottr;
    }
}
