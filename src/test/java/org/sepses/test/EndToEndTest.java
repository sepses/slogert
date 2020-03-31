package org.sepses.test;

import org.apache.commons.cli.ParseException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.sepses.MainParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class EndToEndTest {
    private static final Logger log = LoggerFactory.getLogger(EndToEndTest.class);

    @BeforeClass public static void setup() {

    }

    @Test public void testAuthParser() throws IOException, ParseException {
        File templateFile = new File(GenericParserTest.class.getClassLoader().getResource("auth-template-input.yaml").getFile());
        File configFile = new File(GenericParserTest.class.getClassLoader().getResource("auth-config-input.yaml").getFile());

        if(templateFile.isFile() && configFile.isFile()) {
            String[] params = { "-c " + configFile.getAbsolutePath(), "-t " + templateFile.getAbsolutePath() };
            MainParser.main(params);
        }
    }

    @Test public void testAuditParser() throws IOException, ParseException {
        File templateFile = new File(GenericParserTest.class.getClassLoader().getResource("audit-template.yaml").getFile());
        File configFile = new File(GenericParserTest.class.getClassLoader().getResource("audit-config.yaml").getFile());

        if(templateFile.isFile() && configFile.isFile()) {
            String[] params = { "-c " + configFile.getAbsolutePath(), "-t " + templateFile.getAbsolutePath() };
            MainParser.main(params);
        }
    }

    @Test public void testFtpParser() throws IOException, ParseException {
        File templateFile = new File(GenericParserTest.class.getClassLoader().getResource("ftp-template.yaml").getFile());
        File configFile = new File(GenericParserTest.class.getClassLoader().getResource("ftp-config.yaml").getFile());

        if(templateFile.isFile() && configFile.isFile()) {
            String[] params = { "-c " + configFile.getAbsolutePath(), "-t " + templateFile.getAbsolutePath() };
            MainParser.main(params);
        }
    }

    @Test public void testApacheParser() throws IOException, ParseException {
        File templateFile = new File(GenericParserTest.class.getClassLoader().getResource("apache-template.yaml").getFile());
        File configFile = new File(GenericParserTest.class.getClassLoader().getResource("apache-config.yaml").getFile());

        if(templateFile.isFile() && configFile.isFile()) {
            String[] params = { "-c " + configFile.getAbsolutePath(), "-t " + templateFile.getAbsolutePath() };
            MainParser.main(params);
        }
    }
}
