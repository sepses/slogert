package org.sepses.test;

import org.apache.commons.cli.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sepses.MainParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class IntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(IntegrationTest.class);
    private static ClassLoader classLoader;

    @BeforeClass
    public static void setup() {
        classLoader = IntegrationTest.class.getClassLoader();
    }

    @Test
    public void testMailInfoParser() throws IOException, ParseException {
        File configFile = new File(classLoader.getResource("universal-config.yaml").getFile());

        if (configFile.isFile()) {
            String[] params = {"-c " + configFile.getAbsolutePath()};
            MainParser.main(params);
        }
    }
}
