package org.sepses.test;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sepses.yaml.Config;
import org.sepses.yaml.InternalConfig;
import org.sepses.yaml.YamlFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

public class YamlParserTest {
    private static final Logger log = LoggerFactory.getLogger(YamlParserTest.class);

    private static InternalConfig iConfig;
    private static Config config;

    @BeforeClass public static void setup() {
        InputStream iConfigIS = YamlParserTest.class.getClassLoader().getResourceAsStream("slogert.yaml");
        Yaml yaml = new Yaml(new Constructor(InternalConfig.class));
        iConfig = yaml.load(iConfigIS);
    }

    @Test public void testYamlParser1() {
        InputStream configIS = YamlParserTest.class.getClassLoader().getResourceAsStream("auth-config.yaml");
        InputStream templateIS = YamlParserTest.class.getClassLoader().getResourceAsStream("auth-template.yaml");
        InputStream is = new SequenceInputStream(configIS, templateIS);
        Yaml yaml = new Yaml(new Constructor(Config.class));
        config = yaml.load(is);
        config.internalLogType =
                iConfig.logTypes.stream().filter(item -> item.id.equals(config.logType)).findFirst().get();
        System.out.println(config.parameters.get(0).regexNer.action);
    }

    @Test public void testYamlNerConstruct() throws IOException {
        InputStream configIS = YamlParserTest.class.getClassLoader().getResourceAsStream("auth-config.yaml");
        InputStream templateIS = YamlParserTest.class.getClassLoader().getResourceAsStream("auth-template.yaml");
        InputStream is = new SequenceInputStream(configIS, templateIS);
        Yaml yaml = new Yaml(new Constructor(Config.class));
        config = yaml.load(is);
        config.internalLogType =
                iConfig.logTypes.stream().filter(item -> item.id.equals(config.logType)).findFirst().get();
        YamlFunction.constructRegexNer(config);
    }

    @Test public void testYamlOttrConstruct() throws IOException {
        InputStream configIS = YamlParserTest.class.getClassLoader().getResourceAsStream("auth-config.yaml");
        InputStream templateIS = YamlParserTest.class.getClassLoader().getResourceAsStream("auth-template.yaml");
        InputStream is = new SequenceInputStream(configIS, templateIS);
        Yaml yaml = new Yaml(new Constructor(Config.class));
        config = yaml.load(is);
        config.internalLogType =
                iConfig.logTypes.stream().filter(item -> item.id.equals(config.logType)).findFirst().get();
        YamlFunction.constructOttrTemplate(config);
    }

    @Test public void testInternalConfig() throws IOException {
        InputStream configIS = YamlParserTest.class.getClassLoader().getResourceAsStream("slogert.yaml");
        Yaml yaml = new Yaml(new Constructor(InternalConfig.class));
        InternalConfig config = yaml.load(configIS);

        config.logTypes.stream().forEach(item -> {
            log.info("type: " + item.id);
            log.info("classPath: " + item.classPath);
            log.info("header: " + item.header);
        });
    }

}
