package org.sepses.rdf;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * TODO: this class is not complete yet!
 */
public class Slogert {
    /**
     * <p>The namespace of the vocabulary as a string</p>
     */
    public static final String NS = "http://w3id.org/sepses/vocab/log/slogert#";
    public static final String NS_INSTANCE = "http://w3id.org/sepses/id/slogert/";
    /**
     * <p>The RDF model that holds the vocabulary terms</p>
     */
    private static final Model M_MODEL = ModelFactory.createDefaultModel();
    /**
     * <p>The namespace of the vocabulary as a resource</p>
     */
    public static final Resource NAMESPACE = M_MODEL.createResource(NS);
    public static final Property annotateAction =
            M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#annotateAction");
    public static final Property classPath =
            M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#classPath");
    public static final Property contained =
            M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#contained");
    public static final Property csvColumn =
            M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#csvColumn");
    public static final Property csvColumnName =
            M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#csvColumnName");
    public static final Property csvHeader =
            M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#csvHeader");
    public static final Property example = M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#example");
    public static final Property hasClass =
            M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#hasClass");
    public static final Property hasLogConfig =
            M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#hasLogConfig");
    public static final Property hasParameterList =
            M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#hasParameterList");
    public static final Property hasProperty =
            M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#hasProperty");
    public static final Property hasSuggestedRange =
            M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#hasSuggestedRange");
    public static final Property hasTargetClass =
            M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#hasTargetClass");
    public static final Property id = M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#id");
    public static final Property keyword = M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#keyword");
    public static final Property logMessage =
            M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#logMessage");
    public static final Property logType = M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#logType");
    public static final Property origin = M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#origin");
    public static final Property pattern = M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#pattern");
    public static final Property regexPattern =
            M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#regexPattern");
    public static final Property templateHash =
            M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#templateHash");
    public static final Property templateId =
            M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#templateId");
    public static final Property timestamp =
            M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#timestamp");
    public static final Property turtleOutput =
            M_MODEL.createProperty("http://w3id.org/sepses/vocab/log/slogert#turtleOutput");
    public static final Resource InputParameter =
            M_MODEL.createResource("http://w3id.org/sepses/vocab/log/slogert#InputParameter");
    public static final Resource InternalParameter =
            M_MODEL.createResource("http://w3id.org/sepses/vocab/log/slogert#InternalParameter");
    public static final Resource LogConfig =
            M_MODEL.createResource("http://w3id.org/sepses/vocab/log/slogert#LogConfig");
    public static final Resource LogEntry =
            M_MODEL.createResource("http://w3id.org/sepses/vocab/log/slogert#LogEntry");
    public static final Resource NerParameter =
            M_MODEL.createResource("http://w3id.org/sepses/vocab/log/slogert#NerParameter");
    public static final Resource Parameter =
            M_MODEL.createResource("http://w3id.org/sepses/vocab/log/slogert#Parameter");
    public static final Resource Template =
            M_MODEL.createResource("http://w3id.org/sepses/vocab/log/slogert#Template");

    /**
     * <p>The namespace of the vocabulary as a string</p>
     *
     * @return namespace as String
     * @see #NS
     */
    public static String getURI() {
        return NS;
    }

    /**
     * <p>The namespace of the vocabulary as a string</p>
     *
     * @return namespace as String
     * @see #NS
     */
    //    public static String getURI() {
    //        return NS;
    //    }
    //    public static final String ontologyURI = "http://w3id.org/sepses/vocab/log/slogert#";
    //    public static final String NS_INSTANCE = "http://w3id.org/sepses/id/slogert/";
    //
    //    //
    //    public static final Resource NS;
    //    public static final Resource NS_INSTANCE;
    //
    //    // classes
    //    public static final Resource Template;
    //    public static final Resource LogConfig;
    //    public static final Resource Parameter;
    //    public static final Resource InternalParameter;
    //    public static final Resource InputParameter;
    //    public static final Resource NerParameter;
    //
    //    // parameterVariable
    //    public static final Property id;
    //    public static final Property hasClass;
    //    public static final Property classPath;
    //    public static final Property csvHeader;
    //    public static final Property hasProperty;
    //    public static final Property hasSuggestedRange;
    //    public static final Property hasLogConfig;
    //    public static final Property csvColumn;
    //    public static final Property csvColumnName;
    //    public static final Property example;
    //    public static final Property regexPattern;
    //    public static final Property annotateAction;
    //
    //    // template variables
    //    public static final Property templateHash;
    //    public static final Property parameterList;
    //    public static final Property keyword;
    //    public static final Property ottrID;
    //    public static final Property logType;
    //    public static final Property logTurtle;
    //    public static final Property targetClass;
    //    public static final Property pattern;
    //
    //    // root variable
    //    public static final Property containedVariable;
    //    public static final Property origin;
    //
    //    // ner variables
    //    public static final Property containedIp;
    //    public static final Property containedPort;
    //    public static final Property containedHost;
    //    public static final Property containedDomain;
    //    public static final Property containedUser;
    //
    //    // non-ner variables
    //    public static final Property containedFilePath;
    //    public static final Property containedFile;
    //    public static final Property containedPath;
    //
    //    private static final Model m = ModelFactory.createDefaultModel();
    //
    //    static {
    //
    //        NS = m.createResource(ontologyURI);
    //        NS_INSTANCE = m.createResource(NS_INSTANCE);
    //
    //        Template = m.createResource("http://w3id.org/sepses/vocab/log/slogert#Template");
    //
    //        templateHash = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#templateHash");
    //        parameterList = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#parameterList");
    //        keyword = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#keyword");
    //        ottrID = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#ottrID");
    //        logType = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#logType");
    //        logTurtle = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#logTurtle");
    //        targetClass = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#targetClass");
    //        pattern = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#pattern");
    //
    //        containedVariable = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedVariable");
    //        origin = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#origin");
    //
    //        containedIp = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedIp");
    //        containedPort = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedPort");
    //        containedHost = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedHost");
    //        containedDomain = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedDomain");
    //        containedUser = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedUser");
    //
    //        containedFilePath = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedFilePath");
    //        containedFile = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedFile");
    //        containedPath = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#containedPath");
    //
    //        LogConfig = m.createResource("http://w3id.org/sepses/vocab/log/slogert#LogConfig");
    //        Parameter = m.createResource("http://w3id.org/sepses/vocab/log/slogert#Parameter");
    //        InternalParameter = m.createResource("http://w3id.org/sepses/vocab/log/slogert#InternalParameter");
    //        InputParameter = m.createResource("http://w3id.org/sepses/vocab/log/slogert#InputParameter");
    //        NerParameter = m.createResource("http://w3id.org/sepses/vocab/log/slogert#NerParameter");
    //
    //        // parameterVariable
    //        id = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#id");
    //        hasClass = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#hasClass");
    //        classPath = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#classPath");
    //        csvHeader = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#csvHeader");
    //        hasProperty = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#hasProperty");
    //        hasSuggestedRange = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#hasSuggestedRange");
    //        hasLogConfig = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#hasLogConfig");
    //        csvColumn = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#csvColumn");
    //        csvColumnName = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#csvColumnName");
    //        example = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#example");
    //        regexPattern = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#regexPattern");
    //        annotateAction = m.createProperty("http://w3id.org/sepses/vocab/log/slogert#annotateAction");
    //
    //    }
    //
    //    public Slogert() {
    //    }
    //
    //    public static String getURI() {
    //        return "http://w3id.org/sepses/vocab/log/slogert";
    //    }
}
