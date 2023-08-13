public class SigAnnotationDelta extends SigDelta<IAnnotation> implements
        IAnnotationDelta {
    private Set<IAnnotationElementDelta> annotationElementDeltas;
    public SigAnnotationDelta(IAnnotation from, IAnnotation to) {
        super(from, to);
    }
    public Set<IAnnotationElementDelta> getAnnotationElementDeltas() {
        return annotationElementDeltas;
    }
    public void setAnnotationElementDeltas(
            Set<IAnnotationElementDelta> annotationElementDeltas) {
        this.annotationElementDeltas = annotationElementDeltas;
    }
}
