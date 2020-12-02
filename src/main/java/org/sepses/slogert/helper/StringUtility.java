package org.sepses.slogert.helper;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtility {

    private static final Logger log = LoggerFactory.getLogger(StringUtility.class);

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

    public static String cleanParameter(String input) {
        String output = input.trim();

        Pattern pattern = Pattern.compile("(^\\w+=\\\"*\\'*)([A-Za-z0-9.\\-:_\\/]+)");
        Matcher matcher = pattern.matcher(output);
        while (matcher.find()) {
            output = matcher.group(2);
        }

        output = output.replaceAll("'", ""); // remove all single quote
        output = output.replaceAll("\"", ""); // remove all double quote

        output = output.replaceAll(",", ""); // remove all commas in the beginning or end
        output = output.replaceAll("\\\\", "\\\\\\\\"); // replace backslash with double backslash

        return output;
    }

    public static String getIp(String input) {
        String output = input.trim();

        Pattern pattern = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");
        Matcher matcher = pattern.matcher(output);
        while (matcher.find()) {
            output = matcher.group();
        }

        return output;
    }

}
