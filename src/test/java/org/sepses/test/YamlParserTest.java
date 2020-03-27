package org.sepses.test;

import org.junit.Test;
import org.sepses.yaml.Config;
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

    @Test public void testYamlParser1() {
        InputStream configIS = YamlParserTest.class.getClassLoader().getResourceAsStream("slogert-config.yaml");
        InputStream templateIS = YamlParserTest.class.getClassLoader().getResourceAsStream("slogert-template.yaml");
        InputStream is = new SequenceInputStream(configIS, templateIS);
        Yaml yaml = new Yaml(new Constructor(Config.class));
        Config config = yaml.load(is);
        System.out.println(config.parameters.get(0).regexNer.action);
    }

    @Test public void testYamlNerConstruct() throws IOException {
        InputStream configIS = YamlParserTest.class.getClassLoader().getResourceAsStream("slogert-config.yaml");
        InputStream templateIS = YamlParserTest.class.getClassLoader().getResourceAsStream("slogert-template.yaml");
        InputStream is = new SequenceInputStream(configIS, templateIS);
        Yaml yaml = new Yaml(new Constructor(Config.class));
        Config config = yaml.load(is);
        YamlFunction.constructRegexNer(config);
    }

    @Test public void testYamlOttrConstruct() throws IOException {
        InputStream configIS = YamlParserTest.class.getClassLoader().getResourceAsStream("slogert-config.yaml");
        InputStream templateIS = YamlParserTest.class.getClassLoader().getResourceAsStream("slogert-template.yaml");
        InputStream is = new SequenceInputStream(configIS, templateIS);
        Yaml yaml = new Yaml(new Constructor(Config.class));
        Config config = yaml.load(is);
        YamlFunction.constructOttrTemplate(config);
    }

}
