package org.sepses.slogert.rdf;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class LOGEX {
    /**
     * <p>The namespace of the vocabulary as a string</p>
     */
    public static final String NS = "https://w3id.org/sepses/ns/logex#";
    public static final String NS_INSTANCE = "https://w3id.org/sepses/id/logex/";
    public static final String NS_PREFIX = "logex";
    public static final String NS_INSTANCE_PREFIX = "lxid";
    /**
     * <p>The RDF model that holds the vocabulary terms</p>
     */
    private static final Model M_MODEL = ModelFactory.createDefaultModel();
    /**
     * <p>The namespace of the vocabulary as a resource</p>
     */
    public static final Resource NAMESPACE = M_MODEL.createResource(NS);

    public static final Property ottrBaseTemplate =
            M_MODEL.createProperty("https://w3id.org/sepses/ns/logex#ottrBaseTemplate");
    public static final Property pattern = M_MODEL.createProperty("https://w3id.org/sepses/ns/logex#pattern");
    public static final Property example = M_MODEL.createProperty("https://w3id.org/sepses/ns/logex#example");
    public static final Property keyword = M_MODEL.createProperty("https://w3id.org/sepses/ns/logex#keyword");
    public static final Property isLogEventTemplateOf =
            M_MODEL.createProperty("https://w3id.org/sepses/ns/logex#isLogEventTemplateOf");
    public static final Property hasParameterList =
            M_MODEL.createProperty("https://w3id.org/sepses/ns/logex#hasParameterList");
    public static final Property associatedLogSourceType =
            M_MODEL.createProperty("https://w3id.org/sepses/ns/logex#associatedLogSourceType");

    public static final Resource OttrTemplate =
            M_MODEL.createResource("https://w3id.org/sepses/ns/logex#OttrTemplate");
    public static final Resource LogEventTemplate =
            M_MODEL.createResource("https://w3id.org/sepses/ns/logex#LogEventTemplate");
    public static final Resource LogFormatTemplate =
            M_MODEL.createResource("https://w3id.org/sepses/ns/logex#LogFormatTemplate");
}
