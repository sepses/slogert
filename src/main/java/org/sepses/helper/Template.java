package org.sepses.helper;

import org.apache.commons.csv.CSVRecord;
import org.sepses.nlp.EntityRecognition;
import org.sepses.yaml.Config;
import org.sepses.yaml.ConfigParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Template {

    public static final String UNKNOWN_PARAMETER = "Unknown";
    public static final String BASE_OTTR_ID = "id:Template_";

    private static final Logger log = LoggerFactory.getLogger(Template.class);
    private final String BASE_HEADER =
            "[ottr:IRI ?id, xsd:datetime ?timeStamp, xsd:string ?message, xsd:string ?templateHash";
    private final String BASE_CONTENT = "\n\t id:BasicLog(?id, ?timeStamp, ?message, ?templateHash)";
    private final Config config;
    private final HashMap<String, ConfigParameter> parameterMap = new HashMap<>();

    // private final String[] TEMPLATE_HEADERS = { "logpai_id", "hash", "content", "template", "keywords" };

    public String hash;
    public String templateText;
    public String ottrId;
    public String ottrTemplate;
    public List<String> parameters;
    public List<String> keywords;

    private Template(String hash, String templateText, Config config) {
        this.hash = hash;
        this.templateText = templateText;
        this.config = config;
        config.parameters.forEach(parameter -> {
            parameterMap.put(parameter.id, parameter);
        });
        parameters = new ArrayList<>();
        keywords = new ArrayList<>();
    }

    public Template(String hash, String templateText, String ottrId, String parameters, String keywords,
            Config config) {
        this(hash, templateText, config);
        this.ottrId = ottrId;
        setParameters(parameters);
        setKeywords(keywords);
        generateOttrTemplate();
    }

    public Template(String templateText, String templateHash, LogLine logLine, Config config) {
        this(templateHash, templateText, config);

        // *** set ottrId
        String ottrTemplateName = BASE_OTTR_ID + hash;
        ottrId = ottrTemplateName;

        // *** set keyword
        EntityRecognition er = EntityRecognition.getInstance(config.targetNer);
        keywords = er.extractKeywords(templateText);

        // *** set & process parameters
        HashMap<String, String> matchedExpressions = er.annotateSentence(logLine.getContent());
        int paramSize = logLine.getParameters().size();
        if (paramSize > 0) {
            for (int counter = 0; counter < paramSize; counter++) {
                String param = logLine.getParameters().get(counter);
                if (matchedExpressions.containsKey(param)) {
                    String type = matchedExpressions.get(param);
                    parameters.add(type);
                } else {
                    parameters.add(UNKNOWN_PARAMETER);
                }
            }
        }

        // *** generate OTTR template
        generateOttrTemplate();
    }

    public static Template parseExistingTemplate(CSVRecord record, Config config) {
        return new Template(record.get(0), record.get(1), record.get(2), record.get(3), record.get(4), config);
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
     */
    private void generateOttrTemplate() {

        // *** initialize ottrSB
        StringBuilder ottrHeader = new StringBuilder();
        StringBuilder ottrContent = new StringBuilder();
        ottrHeader.append(ottrId);
        ottrHeader.append(BASE_HEADER);
        ottrContent.append(BASE_CONTENT);

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
                } else if (!parameter.ottr.ottrType.equals(Utility.OTTR_IRI)) { // datatype property
                    ottrContent.append("\n\t id:" + paramValue + "(?id, ?param" + counter + "),");
                    ottrHeader.append(", " + parameter.ottr.ottrProperty + " ?param" + counter);
                } else { // object property
                    ottrContent.append("\n\t id:" + paramValue + "(?id, ?param" + counter + "),");
                    ottrHeader.append(", " + Utility.OTTR_IRI + " ?param" + counter);
                }
            }
            ottrContent.deleteCharAt(ottrContent.length() - 1);
        }
        ottrHeader.append("] :: {");
        ottrContent.append("\n} . \n");

        ottrTemplate = ottrHeader.append(ottrContent).toString();
    }

}
