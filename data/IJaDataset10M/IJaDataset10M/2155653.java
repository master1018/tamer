package net.sf.gaboto.vocabulary;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.*;

/**
 * Vocabulary definitions from ontologies/DCTerms.owl. 
 * 
 * @author Auto-generated by net.sf.gaboto.generation.VocabularyGenerator 
 */
public class DCTermsVocab {

    /** <p>The ontology model that holds the vocabulary terms</p> */
    public static OntModel MODEL = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);

    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://purl.org/dc/terms/";

    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {
        return NS;
    }

    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = MODEL.createResource(NS);

    /** <p>This term is intended to be used with non-literal values as defined in the 
     *  DCMI Abstract Model (http://dublincore.org/documents/abstract-model/). As 
     *  of December 2007, the DCMI Usage Board is seeking a way to express this intention 
     *  with a formal range declaration.</p>
     */
    public static final String isPartOf_URI = "http://purl.org/dc/terms/isPartOf";

    public static final AnnotationProperty isPartOf = MODEL.createAnnotationProperty("http://purl.org/dc/terms/isPartOf");
}