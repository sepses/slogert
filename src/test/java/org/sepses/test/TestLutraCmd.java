package org.sepses.test;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TestLutraCmd {

    @Ignore
    @Test public void lutraCmd() throws IOException {

        File outputFile = new File("scenario/output/test.ttl");
        File outputError = new File("scenario/output/test.log");

        ProcessBuilder pb = new ProcessBuilder("time","java", "-jar", "executable/lutra.jar"
                , "--library", "scenario/output/auth-base.ottr", "--libraryFormat", "stottr","--inputFormat", "stottr",
                "scenario/output/auth.ottr", "--mode", "expand", "--fetchMissing");
        pb.redirectOutput(ProcessBuilder.Redirect.to(outputFile));
        pb.redirectError(ProcessBuilder.Redirect.to(outputError));
        Process p = pb.start();
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals("If redirected, should be -1 ", -1, p.getInputStream().read());
    }
}
