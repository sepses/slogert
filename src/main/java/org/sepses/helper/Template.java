package org.sepses.helper;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.sepses.nlp.EntityRecognition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Template {

    private static final Logger log = LoggerFactory.getLogger(Template.class);

    public static String[] TEMPLATE_HEADERS = { "logpai_id", "hash", "content", "template", "keywords" };

    private final String BASE_OTTR_ID = "sepses:LogLine_";

    public String hash;
    public String templateText;
    public String ottrId;
    public List<ParameterType> parameters;
    public List<String> keywords;
    public String ottrTemplate;

    private Template(String hash, String templateText) {
        this.hash = hash;
        this.templateText = templateText;
        parameters = new ArrayList<>();
        keywords = new ArrayList<>();
    }

    public Template(String hash, String templateText, String ottrId, String parameters, String keywords) {
        this(hash, templateText);
        this.ottrId = ottrId;
        setParameters(parameters);
        setKeywords(keywords);
        generateOttrTemplate();
    }

    public Template(String templateText, String templateHash, LogLine logLine) {
        this(templateHash, templateText);

        // *** set ottrId
        String ottrTemplateName = BASE_OTTR_ID + hash;
        ottrId = ottrTemplateName;

        // *** set keyword
        EntityRecognition er = EntityRecognition.getInstance();
        String[] keywords = er.extractKeywords(templateText);
        setKeywords(StringUtils.join(keywords, '|'));

        // *** set & process parameters
        HashMap<String, String> matchedExpressions = er.annotateSentence(logLine.getContent());
        int paramSize = logLine.getParameters().size();
        if (paramSize > 0) {
            for (int counter = 0; counter < paramSize; counter++) {
                String param = logLine.getParameters().get(counter);
                if (matchedExpressions.containsKey(param)) {
                    String type = matchedExpressions.get(param);
                    parameters.add(ParameterType.valueOf(type));
                } else {
                    parameters.add(ParameterType.Unknown);
                }
            }
        }

        // *** generate OTTR template
        generateOttrTemplate();
    }

    public static Map<String, Template> loadTemplates(Iterable<CSVRecord> existingTemplates) {
        Map<String, Template> templatesMap = new HashMap<>();
        existingTemplates.forEach(existingTemplate -> {
            Template template = Template.parseExistingTemplate(existingTemplate);
            templatesMap.put(template.hash, template);
        });

        return templatesMap;
    }

    public static Template parseExistingTemplate(CSVRecord record) {
        return new Template(record.get(0), record.get(1), record.get(2), record.get(3), record.get(4));
    }

    public void setParameters(String parameters) {
        if (!parameters.isEmpty()) {
            List<String> paramStrings = Arrays.asList(parameters.split("|"));
            paramStrings.forEach(param -> this.parameters.add(ParameterType.valueOf(param)));
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
        ottrHeader.append(ottrId);
        ottrHeader.append("[ottr:IRI ?id, xsd:datetime ?timeStamp, xsd:string ?message, xsd:string ?templateHash");

        StringBuilder ottrContent = new StringBuilder();
        ottrContent.append("\n\t sepses:BasicLogLineInformation(?id, ?timeStamp, ?message, ?templateHash),");
        ottrContent.append("\n\t sepses:Type(?id, :Logline)");

        // *** create ottr
        int parameterCount = parameters.size();
        if (parameterCount > 0) {
            ottrContent.append(","); // add comma for ottr template
            for (int counter = 0; counter < parameterCount; counter++) {
                ParameterType paramValue = parameters.get(counter);
                String paramString = paramValue.toString();
                if (paramValue.equals(ParameterType.Unknown)) {
                    ottrContent.append("\n\t sepses:UnknownConnection" + "(?id, ?param" + counter + "),");
                    ottrHeader.append(", xsd:string ?param" + counter);
                } else { // object property
                    ottrContent.append("\n\t sepses:" + paramString + "(?id, ?param" + counter + "),");
                    ottrHeader.append(", ottr:IRI ?param" + counter);
                }
            }
            ottrContent.deleteCharAt(ottrContent.length() - 1);
        }
        ottrHeader.append("] :: {");
        ottrContent.append("\n} . \n");

        ottrTemplate = ottrHeader.append(ottrContent).toString();
    }

}
