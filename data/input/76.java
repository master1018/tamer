public class EntityLabel implements Feature {
    public Object get(Object object, Structure structure) {
        MyOntology ontology = (MyOntology) structure;
        OWLEntity entity = (OWLEntity) object;
        try {
            String label = (String) entity.getAnnotationValue(ontology.ontology, ontology.owlxlabel);
            return label;
        } catch (Exception e) {
            try {
                StringWithLanguage label2 = (StringWithLanguage) entity.getAnnotationValue(ontology.ontology, ontology.owlxlabel);
                return label2.getString();
            } catch (Exception e2) {
                return null;
            }
        }
    }
}
