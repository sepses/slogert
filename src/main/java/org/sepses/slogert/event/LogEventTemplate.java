package org.sepses.slogert.event;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.sepses.slogert.config.ExtractionConfig;
import org.sepses.slogert.config.Parameter;
import org.sepses.slogert.helper.JenaUtility;
import org.sepses.slogert.ottr.OttrTemplate;
import org.sepses.slogert.rdf.LOG;
import org.sepses.slogert.rdf.LOGEX;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

public class LogEventTemplate {

    public static String CEE_FILE = "cee-taxonomy.ttl";
    public static Model CEE_MODEL = ModelFactory.createDefaultModel();

    public String label;
    public String pattern;
    public String example;
    public List<String> logSourceTypes;
    public List<String> keywords;
    public List<String> ceeAnnotations;
    public List<String> parameters;
    public Integer extractionCount;

    public LogEventTemplate() {
        logSourceTypes = new ArrayList<>();
        keywords = new ArrayList<>();
        parameters = new ArrayList<>();
        ceeAnnotations = new ArrayList<>();
    }

    public static Map<String, LogEventTemplate> fromModel(Model model, ExtractionConfig config) {
        Map<String, LogEventTemplate> templateMap = new HashMap<>();

        model.listSubjectsWithProperty(RDF.type, LOGEX.LogEventTemplate).forEachRemaining(ind -> {
            LogEventTemplate let = new LogEventTemplate();

            String label = ind.getProperty(RDFS.label).getString();
            String pattern = ind.getProperty(LOGEX.pattern).getString();
            String example = ind.getProperty(LOGEX.example).getString();
            StmtIterator keywords = ind.listProperties(LOGEX.keyword);
            StmtIterator sourceTypes = ind.listProperties(LOGEX.associatedLogSourceType);
            RDFList paramList = ind.getProperty(LOGEX.hasParameterList).getObject().as(RDFList.class);
            paramList.asJavaList().forEach(item -> let.parameters.add(item.toString()));

            let.label = label;
            let.pattern = pattern;
            let.example = example;
            let.extractionCount = config.paramExtractAttempt;

            keywords.forEachRemaining(item -> let.keywords.add(item.getObject().toString()));
            sourceTypes.forEachRemaining(item -> {
                String type = item.getObject().toString().split("_")[1];
                let.logSourceTypes.add(type);
            });

            templateMap.put(label, let);

        });

        return templateMap;
    }

    /**
     * generate OTTR template internally
     */
    public OttrTemplate generateOttrTemplate(ExtractionConfig config) {

        OttrTemplate ottr = new OttrTemplate();
        ottr.uri = JenaUtility.getPrefixedName(LOGEX.LogEventTemplate, LOGEX.NS_INSTANCE_PREFIX, label);
        ottr.appendTemplate(config.logFormatOttrBase);

        parameters.forEach(p -> {
            String ottrTemplateUri = JenaUtility.getPrefixedName(LOGEX.OttrTemplate, LOGEX.NS_INSTANCE_PREFIX, p);
            Stream<Parameter> stream = Stream.concat(config.nerParameters.stream(), config.nonNerParameters.stream());
            Parameter cp = stream.filter(np -> p.equals(np.id)).findAny().orElse(null);
            if (cp == null) {
                OttrTemplate unknownTemplate =
                        config.ottrTemplates.stream().filter(ot -> ot.uri.equals(ottrTemplateUri)).findFirst()
                                .orElse(null);
                ottr.appendTemplate(unknownTemplate);
            } else {
                ottr.appendTemplate(cp.ottrTemplate);
            }
        });

        return ottr;
    }

    public Model toModel() {

        Model model = ModelFactory.createDefaultModel();

        Resource resource = JenaUtility.createResource(LOGEX.LogEventTemplate, LOGEX.NS_INSTANCE, label);

        model.add(resource, RDF.type, LOGEX.LogEventTemplate);
        model.add(resource, RDFS.label, label);
        model.add(resource, LOGEX.pattern, pattern);
        model.add(resource, LOGEX.example, example);

        RDFList paramList = model.createList();
        for (String parameter : parameters) {
            RDFNode node = model.createLiteral(parameter);
            if (paramList.isEmpty()) {
                paramList = paramList.with(node);
            } else {
                paramList.add(node);
            }
        }
        model.add(resource, LOGEX.hasParameterList, paramList);

        keywords.forEach(keyword -> {
            model.add(resource, LOGEX.keyword, keyword);
            annotateWithCEE(model, resource, keyword);
        });

        logSourceTypes.forEach(lst -> {
            Resource sourceTypeRes = JenaUtility.createResource(LOG.SourceType, LOG.NS_INSTANCE, lst);
            model.add(resource, LOGEX.associatedLogSourceType, sourceTypeRes);
        });

        return model;
    }

    public void annotateWithCEE(Model model, Resource resource, String keyword) {
        if (CEE_MODEL.isEmpty()) { // load cee model
            InputStream is = LogEventTemplate.class.getClassLoader().getResourceAsStream(CEE_FILE);
            RDFDataMgr.read(CEE_MODEL, is, Lang.TURTLE);
        }

        ResIterator skosConcepts = CEE_MODEL.listSubjectsWithProperty(RDF.type, SKOS.Concept);
        skosConcepts.forEachRemaining(concept -> {
            String label = concept.getProperty(RDFS.label).getObject().toString();
            if(keyword.contains(label)){
                model.add(resource, LOGEX.hasAnnotation, concept);
            }
        });
    }
}
