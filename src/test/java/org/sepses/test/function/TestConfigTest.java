package org.sepses.test.function;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

public class TestConfigTest {

    private static final Logger log = LoggerFactory.getLogger(TestConfigTest.class);

    @Test public void TestLogFormatLoader() {
        InputStream is = TestConfigTest.class.getClassLoader().getResourceAsStream("function/test-config.yaml");
        Yaml yaml = new Yaml(new Constructor(TestConfig.class));
        TestConfig testConfig = yaml.load(is);
    }
}
