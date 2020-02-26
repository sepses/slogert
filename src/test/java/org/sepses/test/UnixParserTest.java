package org.sepses.test;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.sepses.helper.Utility;
import org.sepses.processor.Parser;
import org.sepses.processor.UnixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import static org.sepses.helper.Utility.writeToFile;

public class UnixParserTest {

    private static final Logger log = LoggerFactory.getLogger(UnixParserTest.class);

    //    static String dataFileString = "authlog_structured.csv";
    //    static String templateFileString = "authlog_templates.csv";
    //    static String outputMapping = "output-mapping.ottr";
    //    static String outputData = "output-data.ottr";
    static File templateFile;
    static File dataFile;
    static Parser parser;

    @BeforeClass public static void setup() throws IOException {
        //        templateFile = new File(UnixParserTest.class.getClassLoader().getResource(templateFileString).getFile());
        //        dataFile = new File(UnixParserTest.class.getClassLoader().getResource(dataFileString).getFile());
        //        parser = new UnixParser(templateFile.getAbsolutePath(), dataFile.getAbsolutePath(), true);
    }

    @Test public void testAuthParser() throws IOException {
        templateFile = new File(UnixParserTest.class.getClassLoader().getResource("authlog_templates.csv").getFile());
        dataFile = new File(UnixParserTest.class.getClassLoader().getResource("authlog_structured.csv").getFile());
        parser = new UnixParser(templateFile.getAbsolutePath(), dataFile.getAbsolutePath(), true);

        String authMapping = Utility.generateOttrMap(parser.getHashTemplates(), "UnixOttr.stottr");
        String authData = parser.parseLogpaiData(dataFile.getAbsolutePath());
        writeToFile(authMapping, "auth-mapping.ottr");
        writeToFile(authData, "auth-data.ottr");
    }

    @Ignore @Test public void testSyslogParser() throws IOException {
        templateFile = new File(UnixParserTest.class.getClassLoader().getResource("syslog_templates.csv").getFile());
        dataFile = new File(UnixParserTest.class.getClassLoader().getResource("syslog_structured.csv").getFile());
        parser = new UnixParser(templateFile.getAbsolutePath(), dataFile.getAbsolutePath(), true);

        String authMapping = Utility.generateOttrMap(parser.getHashTemplates(), "UnixOttr.stottr");
        String authData = parser.parseLogpaiData(dataFile.getAbsolutePath());
        writeToFile(authMapping, "syslog-mapping.ottr");
        writeToFile(authData, "syslog-data.ottr");
    }

    @Ignore @Test public void testKernParser() throws IOException {
        templateFile = new File(UnixParserTest.class.getClassLoader().getResource("kern_templates.csv").getFile());
        dataFile = new File(UnixParserTest.class.getClassLoader().getResource("kern_structured.csv").getFile());
        parser = new UnixParser(templateFile.getAbsolutePath(), dataFile.getAbsolutePath(), true);

        String authMapping = Utility.generateOttrMap(parser.getHashTemplates(), "UnixOttr.stottr");
        String authData = parser.parseLogpaiData(dataFile.getAbsolutePath());
        writeToFile(authMapping, "kern-mapping.ottr");
        writeToFile(authData, "kern-data.ottr");
    }

    @Ignore @Test public void testLutra() throws IOException, InterruptedException {

        StringBuilder sbCommand = new StringBuilder();
        sbCommand.append("java -Xmx4096m -jar exe/lutra.jar");
        sbCommand.append(" --library auth-mapping.ottr --libraryFormat stottr ");
        sbCommand.append(" --inputFormat stottr auth-data.ottr ");
        sbCommand.append(" --mode expand --fetchMissing");
        String command = sbCommand.toString();
        System.out.println(command);

        Process proc = Runtime.getRuntime().exec(command);
        proc.waitFor();
        InputStream in = proc.getInputStream();
        InputStream err = proc.getErrorStream();
        byte b[] = new byte[in.available()];
        in.read(b, 0, b.length);
        System.out.println(new String(b));

        byte c[] = new byte[err.available()];
        err.read(c, 0, c.length);
        System.out.println(new String(c));
    }

}
