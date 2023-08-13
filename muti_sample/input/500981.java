public abstract class SigMemberDelta<T extends IField> extends SigDelta<T>
        implements IMemberDelta<T> {
    private Set<IModifierDelta> modifierDeltas;
    private ITypeReferenceDelta<?> typeDelta;
    private Set<IAnnotationDelta> annotationDeltas;
    public SigMemberDelta(T from, T to) {
        super(from, to);
    }
    public Set<IModifierDelta> getModifierDeltas() {
        return modifierDeltas;
    }
    public void setModifierDeltas(Set<IModifierDelta> modifierDeltas) {
        this.modifierDeltas = modifierDeltas;
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
