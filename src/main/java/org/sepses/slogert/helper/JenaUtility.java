package org.sepses.slogert.helper;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.sepses.slogert.config.ExtractionConfig;
import org.sepses.slogert.event.LogEventTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JenaUtility {
    /**
     * Create a Jena Model that already includes all necessary namespaces and prefixes.
     *
     * @param config
     * @return Jena {@link Model}
     */
    public static Model createModel(ExtractionConfig config) {
        Model model = ModelFactory.createDefaultModel();
        config.namespaces.forEach(ns -> model.setNsPrefix(ns.prefix, ns.uri));
        return model;
    }

    /**
     * Create a resource from inputs
     *
     * @param cls
     * @param ns
     * @param label
     * @return @{@link Resource}
     */
    public static Resource createResource(Resource cls, String ns, String label) {
        StringBuilder sb = new StringBuilder();
        sb.append(ns).append(cls.getLocalName()).append("_").append(StringUtility.cleanUriContent(label));
        return ResourceFactory.createResource(sb.toString());
    }

    /**
     * Helper function to create a prefixed resource string
     *
     * @param cls
     * @param prefix
     * @param label
     * @return String
     */
    public static String getPrefixedName(Resource cls, String prefix, String label) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(":").append(cls.getLocalName()).append("_").append(StringUtility.cleanUriContent(label));
        return sb.toString();
    }

    /**
     * Create a default model that contains all necessary prefixes
     *
     * @param config Slogert config
     * @return {@link Model}
     */
    public static Model createModelWithNS(ExtractionConfig config) {
        Model model = ModelFactory.createDefaultModel();
        config.namespaces.forEach(ns -> {
            model.setNsPrefix(ns.prefix, ns.uri);
        });
        return model;
    }

    /**
     * Load existing @{@link LogEventTemplate} map from the config-turtle.ttl (if any)
     *
     * @param config
     * @return
     * @throws IOException
     */
    public static Map<String, LogEventTemplate> getLogEventTemplateMap(ExtractionConfig config) throws IOException {
        Map<String, LogEventTemplate> templates = new HashMap<>();

        // *** load existing hashTemplates
        Model model = createModel(config);
        File configTurtle = new File(config.targetConfigTurtle);
        if (configTurtle.isFile()) {
            FileInputStream fis = new FileInputStream(config.targetConfigTurtle);
            RDFDataMgr.read(model, fis, Lang.TRIG);
            fis.close();
            templates.putAll(LogEventTemplate.fromModel(model, config));
        }
        model.close();

        return templates;
    }
}
