package org.sepses.test;

import org.apache.commons.cli.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sepses.MainParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class AitTest {
    private static final Logger log = LoggerFactory.getLogger(IntegrationTest.class);
    private static ClassLoader classLoader;

    @BeforeClass public static void setup() {
        classLoader = IntegrationTest.class.getClassLoader();

    }

    @Test public void testAuth() throws IOException, ParseException {
        File configFile = new File(classLoader.getResource("ait/ait-auth-config.yaml").getFile());

        if (configFile.isFile()) {
            String[] params = { "-c " + configFile.getAbsolutePath() };
            MainParser.main(params);
        }
    }

    @Test public void testApacheError() throws IOException, ParseException {
        File configFile = new File(classLoader.getResource("ait/apache-error-config.yaml").getFile());

        if (configFile.isFile()) {
            String[] params = { "-c " + configFile.getAbsolutePath() };
            MainParser.main(params);
        }
    }
}
