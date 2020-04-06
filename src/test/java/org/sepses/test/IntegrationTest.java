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
    private static File templateFile;
    private static ClassLoader classLoader;
    private static StringBuilder sb;

    @BeforeClass public static void setup() {
        classLoader = IntegrationTest.class.getClassLoader();
        templateFile = new File(classLoader.getResource("std-template.yaml").getFile());
        sb = new StringBuilder();

    }

    @Test public void testApacheParser() throws IOException, ParseException {
        File configFile = new File(classLoader.getResource("apache-access-config.yaml").getFile());

        if (templateFile.isFile() && configFile.isFile()) {
            String[] params = { "-c " + configFile.getAbsolutePath(), "-t " + templateFile.getAbsolutePath() };
            MainParser.main(params);
        }
    }

    @Test public void testApacheErrorParser() throws IOException, ParseException {
        File configFile = new File(classLoader.getResource("apache-error-config.yaml").getFile());

        if (templateFile.isFile() && configFile.isFile()) {
            String[] params = { "-c " + configFile.getAbsolutePath(), "-t " + templateFile.getAbsolutePath() };
            MainParser.main(params);
        }
    }

    @Test public void testApacheHostParser() throws IOException, ParseException {
        File configFile = new File(classLoader.getResource("apache-host-config.yaml").getFile());

        if (templateFile.isFile() && configFile.isFile()) {
            String[] params = { "-c " + configFile.getAbsolutePath(), "-t " + templateFile.getAbsolutePath() };
            MainParser.main(params);
        }
    }

    @Test public void testAuthParser() throws IOException, ParseException {
        File configFile = new File(classLoader.getResource("auth-config.yaml").getFile());

        if (templateFile.isFile() && configFile.isFile()) {
            String[] params = { "-c " + configFile.getAbsolutePath(), "-t " + templateFile.getAbsolutePath() };
            MainParser.main(params);
        }
    }

    @Test public void testAuditParser() throws IOException, ParseException {
        File configFile = new File(classLoader.getResource("audit-config.yaml").getFile());

        if (templateFile.isFile() && configFile.isFile()) {
            String[] params = { "-c " + configFile.getAbsolutePath(), "-t " + templateFile.getAbsolutePath() };
            MainParser.main(params);
        }
    }

    @Test public void testFtpParser() throws IOException, ParseException {
        File configFile = new File(classLoader.getResource("ftp-config.yaml").getFile());

        if (templateFile.isFile() && configFile.isFile()) {
            String[] params = { "-c " + configFile.getAbsolutePath(), "-t " + templateFile.getAbsolutePath() };
            MainParser.main(params);
        }
    }

    @Test public void testKernParser() throws IOException, ParseException {
        File configFile = new File(classLoader.getResource("kern-config.yaml").getFile());

        if (templateFile.isFile() && configFile.isFile()) {
            String[] params = { "-c " + configFile.getAbsolutePath(), "-t " + templateFile.getAbsolutePath() };
            MainParser.main(params);
        }
    }

    @Test public void testSysParser() throws IOException, ParseException {
        File configFile = new File(classLoader.getResource("sys-config.yaml").getFile());

        if (templateFile.isFile() && configFile.isFile()) {
            String[] params = { "-c " + configFile.getAbsolutePath(), "-t " + templateFile.getAbsolutePath() };
            MainParser.main(params);
        }
    }
}