package org.sepses.nlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.*;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;
import org.sepses.config.Parameter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityRecognition {

    private static String RULE_FILE;
    private static HashMap<String, String> dicCodeToIndex = new LinkedHashMap<>();

    private static EntityRecognition singleton = null;
    private StanfordCoreNLP pipeline = null;
    private CoreMapExpressionExtractor extractor = null;

    private EntityRecognition() {

        Properties pipelineProps = new Properties();

        pipelineProps.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        pipelineProps.setProperty("ner.applyFineGrained", "false");
        pipelineProps.setProperty("ssplit.eolonly", "true");
        pipelineProps.setProperty("tokenize.options", "untokenizable=noneKeep");  // to remove warnings
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
    public static EntityRecognition getInstanceConfig(String ruleFile, List<Parameter> parameters) {
        if (singleton == null || !RULE_FILE.equals(ruleFile)) {
            RULE_FILE = ruleFile;
            // add non-ner pattern to be checked.
            parameters.stream().forEach(item -> {
                dicCodeToIndex.put(item.id, item.pattern);
            });

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

        // Parse sentence without word tokens - in case they dont split well, like with paths
        for (String regexKey : dicCodeToIndex.keySet()) {
            Pattern pattern = Pattern.compile(dicCodeToIndex.get(regexKey));
            Matcher matcher = pattern.matcher(inputSentence);

            while (matcher.find()) {
                String found = matcher.group();
                nerList.put(found, regexKey);

                // Add first group as well
                if (matcher.groupCount() > 0) {
                    found = matcher.group(1);
                    nerList.put(found, regexKey);
                }
            }
        }

        return nerList;
    }

    /**
     * extracting keyword from the template logContent.
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

        List<CoreMap> mapList = exampleSentencesAnnotation.get(CoreAnnotations.SentencesAnnotation.class);
        List<String> keywords = new ArrayList<>();

        if (!mapList.isEmpty()) {
            CoreMap sentence = mapList.get(0);
            List<MatchedExpression> matchedExpressions = extractor.extractExpressions(sentence);

            // Get keyword
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

            for (String word : wordsStr) {
                if (word.split(" ").length > 2) { // if the word is more than 2 words - split!
                    String[] words = word.split(" ");
                    List<String> temp = Arrays.asList(words);
                    temp.stream().forEach(item -> filterKeyword(keywords, item));
                } else {
                    filterKeyword(keywords, word);
                }
            }
        }

        return keywords;
    }

    private void filterKeyword(List<String> keywords, String item) {
        String itemStr = item.toLowerCase().trim();
        if (itemStr.length() > 1)
            keywords.add(itemStr);
    }
}
