package org.sepses.helper;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.sepses.nlp.EntityRecognition;
import org.sepses.rdf.Slogert;
import org.sepses.yaml.Config;
import org.sepses.yaml.ConfigParameter;
import org.sepses.yaml.InternalLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Template {

    public static final String UNKNOWN_PARAMETER = "Unknown";
    public static final String BASE_OTTR_ID = "id:Template_";

    private static final Logger log = LoggerFactory.getLogger(Template.class);

    private final String BASE_HEADER =
            "[ottr:IRI ?id, xsd:datetime ?timeStamp, xsd:string ?message, xsd:string ?templateHash";
    private final String BASE_CONTENT = "\n\t id:BasicLog(?id, ?timeStamp, ?message, ?templateHash)";

    private final Config config;
    private final HashMap<String, ConfigParameter> parameterMap = new HashMap<>();

    public String hash;
    public String templateText;
    public String ottrId;
    public List<String> parameters;
    public List<String> keywords;

    private Template(String hash, String templateText, Config config) {

        this.hash = hash;
        this.templateText = templateText;
        this.config = config;

        config.nerParameters.forEach(item -> parameterMap.put(item.id, item));// add ner parameters
        config.nonNerParameters.forEach(item -> parameterMap.put(item.id, item));// add non-ner parameters

        parameters = new ArrayList<>();
        keywords = new ArrayList<>();
    }

    public Template(String hash, String templateText, String ottrId, String parameters, String keywords,
            Config config) {
        this(hash, templateText, config);
        this.ottrId = BASE_OTTR_ID + ottrId;
        setParameters(parameters);
        setKeywords(keywords);
    }

    public Template(String templateText, String templateHash, LogLine logLine, Config config) {
        this(templateHash, templateText, config);

        // *** set ottrId
        String ottrTemplateName = BASE_OTTR_ID + hash;
        ottrId = ottrTemplateName;

        // *** set keyword
        EntityRecognition er = EntityRecognition.getInstance(config.targetNer, config.nonNerParameters);
        keywords = er.extractKeywords(templateText);

        // *** set & process nerParameters
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
            ottrHeader.append(", " + component.ottr.ottrType + " ?" + component.column);
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
        Resource template = model.createResource(Slogert.instanceURI + hash);

        model.add(template, RDF.type, Slogert.Template);
        model.add(template, Slogert.templateHash, hash);
        model.add(template, Slogert.logType, config.internalLogType.id);
        model.add(template, Slogert.pattern, templateText);
        model.add(template, Slogert.ottrID, ottrId);
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
        model.add(template, Slogert.parameterList, list);

        return model;
    }

    public static Map<String, Template> fromModel(Model model, Config config) {
        Map<String, Template> templateMap = new HashMap<>();

        model.listSubjectsWithProperty(RDF.type, Slogert.Template).forEachRemaining(ind -> {
            RDFNode hashNode = ind.getProperty(Slogert.templateHash).getObject();
            String hash = hashNode.asLiteral().getString();

            RDFNode patternNode = ind.getProperty(Slogert.pattern).getObject();
            String pattern = patternNode.asLiteral().getString();

            String ottrId = ind.getProperty(Slogert.templateHash).getObject().asLiteral().getString();
            RDFList paramList = ind.getProperty(Slogert.parameterList).getObject().as(RDFList.class);
            StmtIterator keywords = ind.listProperties(Slogert.keyword);

            Template template = new Template(hash, pattern, config);
            template.ottrId = ottrId;
            paramList.asJavaList().forEach(item -> template.parameters.add(item.asLiteral().getString()));
            keywords.forEachRemaining(item -> template.keywords.add(item.getObject().asLiteral().getString()));

            templateMap.put(hash, template);

        });

        return templateMap;
    }

}
