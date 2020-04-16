package org.sepses.test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sepses.helper.LogLine;
import org.sepses.helper.Template;
import org.sepses.yaml.Config;
import org.sepses.yaml.InternalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TemplateTest {
    private static final Logger log = LoggerFactory.getLogger(TemplateTest.class);

    private static InternalConfig iConfig;
    private static Config config;

    @BeforeClass public static void setup() {
        InputStream iConfigIS = YamlParserTest.class.getClassLoader().getResourceAsStream("slogert.yaml");
        Yaml yaml = new Yaml(new Constructor(InternalConfig.class));
        iConfig = yaml.load(iConfigIS);

        InputStream configIS = YamlParserTest.class.getClassLoader().getResourceAsStream("ftp-config.yaml");
        InputStream templateIS = YamlParserTest.class.getClassLoader().getResourceAsStream("std-template.yaml");
        InputStream is = new SequenceInputStream(configIS, templateIS);
        yaml = new Yaml(new Constructor(Config.class));

        config = yaml.load(is);
        config.internalLogType =
                iConfig.logTypes.stream().filter(item -> item.id.equals(config.logType)).findFirst().get();
    }

    @Test public void testTemplate1() throws FileNotFoundException {
        String tString = "";

        LogLine logLine = mock(LogLine.class);
        when(logLine.getTemplateHash()).thenReturn("h1");
        when(logLine.getCounter()).thenReturn(1);
        when(logLine.getDateTime()).thenReturn("2020-02-02T10:20:20");
        when(logLine.getContent())
                .thenReturn("new user: name=akep, UID=1000, GID=1000, home=/home/akep, shell=/bin/bash");
        when(logLine.getParameters()).thenReturn(new ArrayList<>());
        when(logLine.getLogpaiEventId()).thenReturn("le1");

        Template template = new Template("template-text", "h1", logLine, config);
        Model model = template.toModel();

        RDFDataMgr.write(new FileOutputStream("test.ttl"), model, RDFFormat.TURTLE);
    }

    @Test public void testTemplate2() throws FileNotFoundException {
        String tString = "";

        LogLine logLine = mock(LogLine.class);
        when(logLine.getTemplateHash()).thenReturn("h1");
        when(logLine.getCounter()).thenReturn(1);
        when(logLine.getDateTime()).thenReturn("2020-02-02T10:20:20");
        when(logLine.getContent()).thenReturn("new user: name=akep, UID=1000, GID=1000");
        when(logLine.getParameters()).thenReturn(new ArrayList<>());
        when(logLine.getLogpaiEventId()).thenReturn("le1");

        LogLine logLine2 = mock(LogLine.class);
        when(logLine2.getTemplateHash()).thenReturn("h2");
        when(logLine2.getCounter()).thenReturn(1);
        when(logLine2.getDateTime()).thenReturn("2020-02-02T10:20:20");
        when(logLine2.getContent()).thenReturn("UID=1000, GID=1000, home=/home/akep, shell=/bin/bash");
        when(logLine2.getParameters()).thenReturn(new ArrayList<>());
        when(logLine2.getLogpaiEventId()).thenReturn("le2");

        Template template = new Template(logLine.getContent(), logLine.getTemplateHash(), logLine, config);
        Template template2 = new Template(logLine.getContent(), logLine.getTemplateHash(), logLine2, config);

        Model model = ModelFactory.createDefaultModel();
        model.add(template.toModel());
        model.add(template2.toModel());

        Map<String, Template> templates = Template.fromModel(model, config);

        templates.values().stream().forEach(item -> {
            if (item.hash == logLine.getTemplateHash()) {
                Assert.assertEquals(item.templateText, logLine.getContent());
            }
            if (item.hash == logLine2.getTemplateHash()) {
                Assert.assertEquals(item.templateText, logLine2.getContent());
            }
        });
    }
}
