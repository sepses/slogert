package org.sepses.helper;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class Utility {

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

    public static String generateOttrMap(Map<String, Template> templateMap, String baseFile) {
        StringBuilder sb = new StringBuilder();

        // *** load template
        InputStream is = Utility.class.getClassLoader().getResourceAsStream(baseFile);
        //        File templateFile = new File(Template.class.getClassLoader().getResource(baseFile).getFile());
        try {
            //            String baseTemplate = FileUtils.readFileToString(templateFile, Charset.defaultCharset());
            String baseTemplate = IOUtils.toString(is, Charset.defaultCharset());
            sb.append(baseTemplate);
            sb.append(System.lineSeparator()).append(System.lineSeparator());

            templateMap.values().stream().forEach(template -> {
                sb.append(template.ottrTemplate);
                sb.append(System.lineSeparator());
            });
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return sb.toString();
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
}
