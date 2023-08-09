public class SigParameterDelta extends SigDelta<IParameter> implements
        IParameterDelta {
    private ITypeReferenceDelta<?> typeDelta;
    private Set<IAnnotationDelta> annotationDeltas;
    public SigParameterDelta(IParameter from, IParameter to) {
        super(from, to);
    }
    public ITypeReferenceDelta<?> getTypeDelta() {
        return typeDelta;
    }
    public void setTypeDelta(ITypeReferenceDelta<?> typeDelta) {
        this.typeDelta = typeDelta;
    }
    public Set<IAnnotationDelta> getAnnotationDeltas() {
        return annotationDeltas;
    }
    public void setAnnotationDeltas(Set<IAnnotationDelta> annotationDeltas) {
        this.annotationDeltas = annotationDeltas;
    }
}
