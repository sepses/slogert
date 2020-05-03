package org.sepses.rdf;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * TODO: this class is not complete yet!
 */
public class Slogert {

    public static final String ontologyURI = "http://w3id.org/sepses/vocab/log/slogert#";
    public static final String instanceURI = "http://w3id.org/sepses/id/slogert/";

    //
    public static final Resource NS;
    public static final Resource NS_INSTANCE;

    // classes
    public static final Resource Template;

    // template variables
    public static final Property templateHash;
    public static final Property parameterList;
    public static final Property keyword;
    public static final Property ottrID;
    public static final Property logType;
    public static final Property pattern;

    // root variable
    public static final Property containedVariable;
    public static final Property origin;

    // ner variables
    public static final Property containedIp;
    public static final Property containedPort;
    public static final Property containedHost;
    public static final Property containedDomain;
    public static final Property containedUser;

    // non-ner variables
    public static final Property containedFilePath;
    public static final Property containedFile;
    public static final Property containedPath;

    private static final Model m = ModelFactory.createDefaultModel();

    static {

        NS = m.createResource(ontologyURI);
        NS_INSTANCE = m.createResource(instanceURI);

        Template = m.createResource("http://w3id.org/sepses/vocab/log/slogert#Template");

        templateHash = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#templateHash");
        parameterList = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#parameterList");
        keyword = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#keyword");
        ottrID = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#ottrID");
        logType = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#logType");
        pattern = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#pattern");

        containedVariable = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedVariable");
        origin = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#origin");

        containedIp = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedIp");
        containedPort = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedPort");
        containedHost = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedHost");
        containedDomain = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedDomain");
        containedUser = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedUser");

        containedFilePath = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedFilePath");
        containedFile = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedFile");
        containedPath = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedPath");

    }

    public Slogert() {
    }

    public static String getURI() {
        return "http://w3id.org/sepses/vocab/log/slogert";
    }
}
