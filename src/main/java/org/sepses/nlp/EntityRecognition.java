package org.sepses.nlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.*;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class EntityRecognition {

    private static String RULE_FILE;

    private static EntityRecognition singleton = null;
    private StanfordCoreNLP pipeline = null;
    private CoreMapExpressionExtractor extractor = null;

    private EntityRecognition() {

        Properties pipelineProps = new Properties();

        pipelineProps.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        pipelineProps.setProperty("ner.applyFineGrained", "false");
        pipelineProps.setProperty("ssplit.eolonly", "true");
        pipeline = new StanfordCoreNLP(pipelineProps);

        // *** set up the TokensRegex pipeline

        // get the rules files
        String[] rulesFiles = new String[1];
        rulesFiles[0] = RULE_FILE;

        // set up an environment with reasonable defaults
        Env env = TokenSequencePattern.getNewEnv();

        // set to case insensitive
        env.setDefaultStringMatchFlags(NodePattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        env.setDefaultStringPatternFlags(Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

        // build the CoreMapExpressionExtractor
        extractor = CoreMapExpressionExtractor.createExtractorFromFiles(env, rulesFiles);
    }

    /**
     * Only create a new instance if the rule file changes.
     *
     * @param ruleFile
     * @return
     */
    public static EntityRecognition getInstance(String ruleFile) {
        if (singleton == null || !RULE_FILE.equals(ruleFile)) {
            RULE_FILE = ruleFile;
            singleton = new EntityRecognition();
        }

        return singleton;
    }

    /**
     * Annotate a sentence according to RULE_FILE.
     *
     * @param inputSentence
     * @return a map of <param-value, param-type>
     */
    public HashMap<String, String> annotateSentence(String inputSentence) {
        Annotation exampleSentencesAnnotation = new Annotation(inputSentence);
        pipeline.annotate(exampleSentencesAnnotation);

        // for each sentence in the input text, run the TokensRegex pipeline
        HashMap<String, String> nerList = new HashMap<>();

        CoreMap sentence = exampleSentencesAnnotation.get(CoreAnnotations.SentencesAnnotation.class).get(0);
        List<MatchedExpression> matchedExpressions = extractor.extractExpressions(sentence);

        for (MatchedExpression nerMatch : matchedExpressions) {
            for (CoreLabel token : nerMatch.getAnnotation().get(CoreAnnotations.TokensAnnotation.class)) {
                if (token.ner() != null) {
                    nerList.put(token.word(), token.ner());
                }
            }
        }

        return nerList;
    }

    /**
     * extracting keywords from the template logContent.
     *
     * @param templateText
     * @return
     */
    public List<String> extractKeywords(String templateText) {
        ArrayList<String> termList = new ArrayList<>();
        String[] parts = templateText.split(" ");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (!part.contains("="))
                termList.add(part);
        }

        String cleanSentence = StringUtils.join(termList.toArray());
        Annotation exampleSentencesAnnotation = new Annotation(cleanSentence.replace("=", ""));

        pipeline.annotate(exampleSentencesAnnotation);

        // for each sentence in the input text, run the TokensRegex pipeline
        HashMap<String, String> nerList = new HashMap<>();

        CoreMap sentence = exampleSentencesAnnotation.get(CoreAnnotations.SentencesAnnotation.class).get(0);
        List<MatchedExpression> matchedExpressions = extractor.extractExpressions(sentence);

        // Get keywords
        List<String> wordsStr = new ArrayList<>();
        List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
        String ngram = "";
        boolean first = true;

        for (int i = 0; i < tokens.size(); i++) {
            //System.out.println("Word: " + tokens.get(i).word() + ": " + tokens.get(i).tag());
            if (tokens.get(i).tag().equals("JJ") || tokens.get(i).tag().startsWith("NN") || tokens.get(i).tag()
                    .startsWith("VB")) {
                if (!first)
                    ngram += " ";

                first = false;
                ngram += tokens.get(i).lemma();
                //System.out.println(tokens.get(i).lemma() + " (" + tokens.get(i).tag() + ") - " + tokens.get(i + 1).lemma() + " (" + tokens.get(i + 1).tag() + ")");
            } else {
                if (!ngram.isEmpty()) {
                    wordsStr.add(ngram);
                }
                first = true;
                ngram = "";
            }
        }

        if (!ngram.isEmpty())
            wordsStr.add(ngram);

        List<String> keywords = new ArrayList<>();
        for (String word : wordsStr) {
            if (word.split(" ").length > 1)
                //System.out.println(word);
                keywords.add(word);
        }

        return keywords;
    }
}
