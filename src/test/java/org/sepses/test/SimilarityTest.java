package org.sepses.test;

import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimilarityTest {

    private static final Logger log = LoggerFactory.getLogger(IntegrationTest.class);
    private static StringSimilarityService service;
    String first = "home/core/Documents";
    String second = "/home/core/Documents";

    @Test public void test1() {
        SimilarityStrategy strategy = new JaroWinklerStrategy();
        service = new StringSimilarityServiceImpl(strategy);
        Double score = service.score(first, second); // Score is 0.90
        log.info(score.toString());

    }

    @Test public void test2() {

        StringMetric metric = StringMetrics.longestCommonSubsequence();
        Float result = metric.compare(first, second); //0.4767
        log.info(result.toString());

        metric = StringMetrics.blockDistance();
        result = metric.compare(first, second); //0.4767
        log.info(result.toString());

        metric = StringMetrics.damerauLevenshtein();
        result = metric.compare(first, second); //0.4767
        log.info(result.toString());

        metric = StringMetrics.dice();
        result = metric.compare(first, second); //0.4767
        log.info(result.toString());

        metric = StringMetrics.dice();
        result = metric.compare(first, second); //0.4767
        log.info(result.toString());

        metric = StringMetrics.jaccard();
        result = metric.compare(first, second); //0.4767
        log.info(result.toString());

        metric = StringMetrics.jaro();
        result = metric.compare(first, second); //0.4767
        log.info(result.toString());

        metric = StringMetrics.mongeElkan();
        result = metric.compare(first, second); //0.4767
        log.info(result.toString());



    }

}
