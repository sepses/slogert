package org.sepses.test.function;

import org.junit.Assert;
import org.junit.Test;
import org.sepses.slogert.config.ExtractionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.io.SequenceInputStream;

public class ConfigExtractionTest {

    private static final Logger log = LoggerFactory.getLogger(ConfigExtractionTest.class);

    @Test public void TestLogFormatLoader() {
        InputStream config = ConfigExtractionTest.class.getClassLoader().getResourceAsStream("config.yaml");
        InputStream io = ConfigExtractionTest.class.getClassLoader().getResourceAsStream("config-io.yaml");
        InputStream is = new SequenceInputStream(config, io);

        Yaml yaml = new Yaml(new Constructor(ExtractionConfig.class));
        ExtractionConfig extractionConfig = yaml.load(is);

        Assert.assertEquals(extractionConfig.targetStanfordNer, "output/auth.log/ner.rules");
    }
}
