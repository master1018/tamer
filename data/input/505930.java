public abstract class SigExecutableMemberDelta<T extends IExecutableMember>
        extends SigDelta<T> implements IExecutableMemberDelta<T> {
    private Set<ITypeReferenceDelta<?>> exceptionDeltas;
    private Set<IModifierDelta> modifierDeltas;
    private Set<ITypeVariableDefinitionDelta> typeVariableDeltas;
    private Set<IAnnotationDelta> annotationDeltas;
    private Set<IParameterDelta> parameterDeltas;
    public SigExecutableMemberDelta(T from, T to) {
        super(from, to);
    }
    public Set<ITypeReferenceDelta<?>> getExceptionDeltas() {
        return exceptionDeltas;
    }
    public void setExceptionDeltas(
            Set<ITypeReferenceDelta<?>> exceptionDeltas) {
        this.exceptionDeltas = exceptionDeltas;
    }
    public Set<IModifierDelta> getModifierDeltas() {
        return modifierDeltas;
    }
    public void setModifierDeltas(Set<IModifierDelta> modifierDeltas) {
        this.modifierDeltas = modifierDeltas;
    }
    public Set<ITypeVariableDefinitionDelta> getTypeVariableDeltas() {
        return typeVariableDeltas;
    }
    public void setTypeVariableDeltas(
            Set<ITypeVariableDefinitionDelta> typeVariableDeltas) {
        this.typeVariableDeltas = typeVariableDeltas;
    }
    public Set<IAnnotationDelta> getAnnotationDeltas() {
        return annotationDeltas;
    }
    public void setAnnotationDeltas(Set<IAnnotationDelta> annotationDeltas) {
        this.annotationDeltas = annotationDeltas;
    }
    public Set<IParameterDelta> getParameterDeltas() {
        return parameterDeltas;
    }
    public void setParameterDeltas(Set<IParameterDelta> parameterDeltas) {
        this.parameterDeltas = parameterDeltas;
    }
}
