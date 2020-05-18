package org.sepses.helper;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.sepses.logline.LogLine;
import org.sepses.nlp.EntityRecognition;
import org.sepses.rdf.Slogert;
import org.sepses.yaml.Config;
import org.sepses.yaml.ConfigParameter;
import org.sepses.yaml.InternalLogType;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Template {

    public static final String UNKNOWN_PARAMETER = "Unknown";
    public static final String BASE_OTTR_ID = "id:Template_";

    private static final Logger log = LoggerFactory.getLogger(Template.class);

    private final String BASE_HEADER =
            "[ottr:IRI ?id, xsd:string ?device, xsd:datetime ?timeStamp, xsd:string ?message, xsd:string ?templateHash";
    private final String BASE_CONTENT = "\n\t id:BasicLog(?id, ?device, ?timeStamp, ?message, ?templateHash)";

    private final Config config;
    private final HashMap<String, ConfigParameter> parameterMap = new HashMap<>();
    private final StringMetric metric = StringMetrics.longestCommonSubsequence();

    public String hash;
    public String templateText;
    public String ottrId;
    public List<String> parameters;
    public List<String> keywords;

    private Template(String hash, String templateText, Config config) {

        this.hash = hash;
        this.ottrId = BASE_OTTR_ID + hash;
        this.templateText = templateText;
        this.config = config;

        config.nerParameters.forEach(item -> parameterMap.put(item.id, item));// add ner parameters
        config.nonNerParameters.forEach(item -> parameterMap.put(item.id, item));// add non-ner parameters

        parameters = new ArrayList<>();
        keywords = new ArrayList<>();
    }

    public Template(String templateText, String templateHash, LogLine logLine, Config config) {
        this(templateHash, templateText, config);

        // *** set keyword
        EntityRecognition er = EntityRecognition.getInstance(config.targetNer, config.nonNerParameters);
        keywords = er.extractKeywords(templateText);

        // *** set & process nerParameters
        HashMap<String, String> matchedExpressions = er.annotateSentence(logLine.getContent());
        int paramSize = logLine.getParameters().size();
        if (paramSize > 0) {
            LevenshteinDistance distance = new LevenshteinDistance();

            paramLoop:
            for (int counter = 0; counter < paramSize; counter++) {
                String param = logLine.getParameters().get(counter);

                if (matchedExpressions.containsKey(param)) {
                    String type = matchedExpressions.get(param);
                    parameters.add(type);
                } else {
                    // fuzzy distance for  unexpected parameters
                    double currentMinimalDistance = 1;
                    String minmalDistanceKey = "";

                    for (Map.Entry<String, String> entry : matchedExpressions.entrySet()) {
                        if (entry.getKey() != null) {
                            String k = entry.getKey();
                            double dist = distance.apply(k, param);
                            double maxLength = ((param.length() > k.length()) ? param.length() : k.length());
                            double relativeDistance = dist / maxLength;

                            if (relativeDistance < currentMinimalDistance) {
                                currentMinimalDistance = relativeDistance;
                                minmalDistanceKey = k;
                            }
                        }
                    }

                    // Relative distance accepted for cases like http://test.com vs //test.com
                    if (currentMinimalDistance < 0.25) {
                        String type = matchedExpressions.get(minmalDistanceKey);
                        parameters.add(type);
                    } else {
                        parameters.add(UNKNOWN_PARAMETER);
                    }
                }
            }
        }
    }

    public static Map<String, Template> fromModel(Model model, Config config) {
        Map<String, Template> templateMap = new HashMap<>();

        model.listSubjectsWithProperty(RDF.type, Slogert.Template).forEachRemaining(ind -> {
            RDFNode hashNode = ind.getProperty(Slogert.templateHash).getObject();
            String hash = hashNode.asLiteral().getString();

            RDFNode patternNode = ind.getProperty(Slogert.pattern).getObject();
            String pattern = patternNode.asLiteral().getString();

            RDFList paramList = ind.getProperty(Slogert.hasParameterList).getObject().as(RDFList.class);
            StmtIterator keywords = ind.listProperties(Slogert.keyword);

            Template template = new Template(hash, pattern, config);
            paramList.asJavaList().forEach(item -> template.parameters.add(item.asLiteral().getString()));
            keywords.forEachRemaining(item -> template.keywords.add(item.getObject().asLiteral().getString()));

            templateMap.put(hash, template);

        });

        return templateMap;
    }

    public void setParameters(String parameters) {
        if (!parameters.isEmpty()) {
            this.parameters = Arrays.asList(parameters.split("|"));
        }
    }

    public void setKeywords(String keywords) {
        if (!keywords.isEmpty())
            this.keywords = Arrays.asList(keywords.split("|"));
    }

    /**
     * generate OTTR template internally
     * TODO: change this hardcode into configuration
     */
    public String generateOttrTemplate() {

        // *** initialize ottrSB
        StringBuilder ottrHeader = new StringBuilder();
        StringBuilder ottrContent = new StringBuilder();
        ottrHeader.append(ottrId);
        ottrHeader.append(BASE_HEADER);
        ottrContent.append(BASE_CONTENT);

        // *** handle special components
        InternalLogType iLogType = config.internalLogType;
        iLogType.components.stream().forEach(component -> {
            ottrHeader.append(", xsd:string ?" + component.column);
            ottrContent.append(", \n\t id:" + component.column + "(?id, ?" + component.column + ")");
        });

        // *** create ottr
        int parameterCount = parameters.size();
        if (parameterCount > 0) {
            ottrContent.append(","); // add comma for ottr template
            for (int counter = 0; counter < parameterCount; counter++) {
                String paramValue = parameters.get(counter);
                ConfigParameter parameter = parameterMap.get(paramValue);

                if (paramValue.equals(UNKNOWN_PARAMETER)) {
                    ottrContent.append("\n\t id:UnknownConnection" + "(?id, ?param" + counter + "),");
                    ottrHeader.append(", xsd:string ?param" + counter);
                } else { // datatype property
                    ottrContent.append("\n\t id:" + paramValue + "(?id, ?param" + counter + "),");
                    ottrHeader.append(", " + parameter.ottr.ottrProperty + " ?param" + counter);
                }
            }
            ottrContent.deleteCharAt(ottrContent.length() - 1);
        }
        ottrHeader.append("] :: {");
        ottrContent.append("\n} . \n");

        return ottrHeader.append(ottrContent).toString();
    }

    public Model toModel() {

        Model model = ModelFactory.createOntologyModel();
        Resource template = model.createResource(Slogert.NS_INSTANCE + hash);

        model.add(template, RDF.type, Slogert.Template);
        model.add(template, Slogert.templateHash, hash);
        model.add(template, Slogert.logType, config.internalLogType.id);
        //        model.add(template, Slogert.targetClass, config.logName);
        model.add(template, Slogert.pattern, templateText);
        //        model.add(template, Slogert.ottrID, ottrId);
        keywords.stream().forEach(keyword -> {
            model.add(template, Slogert.keyword, keyword);
        });
        RDFList list = model.createList();
        for (String value : parameters) {
            RDFNode node = model.createTypedLiteral(value);
            if (list.isEmpty()) {
                list = list.with(node);
            } else {
                list.with(node);
            }

        }
        model.add(template, Slogert.hasParameterList, list);

        return model;
    }

}
