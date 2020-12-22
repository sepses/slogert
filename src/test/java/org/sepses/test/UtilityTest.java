package org.sepses.test;

import org.apache.commons.cli.ParseException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sepses.slogert.helper.DateUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class UtilityTest {
    private static final Logger log = LoggerFactory.getLogger(UtilityTest.class);

    @BeforeClass public static void setup() {

    }

    @Test public void timeConversionTest() throws IOException, ParseException {
        String date = "14/Mar/2020:11:53:50+0100";
        String expectedDate = "2020-03-14T11:53:50";

        String convertedDate = DateUtility.localTimeConversion(date, "dd/MMM/yyyy':'HH:mm:ssZ");
        Assert.assertEquals(expectedDate, convertedDate);
    }
}
