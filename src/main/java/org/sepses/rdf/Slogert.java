package org.sepses.rdf;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * TODO: this class is not complete yet!
 */
public class Slogert {
    public static final String uri = "http://w3id.org/sepses/vocab/log/slogert#";
    public static final Resource NAMESPACE;
    public static final Resource Template;
    public static final Property containedVariable;
    private static final Model m = ModelFactory.createDefaultModel();

    static {
        NAMESPACE = m.createResource("http://w3id.org/sepses/vocab/log/slogert#");
        Template = m.createResource("http://w3id.org/sepses/vocab/log/slogert#Template");
        containedVariable = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedVariable");
    }

    public Slogert() {
    }

    public static String getURI() {
        return "http://www.w3.org/2004/02/skos/core#";
    }
}
