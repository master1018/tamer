public class ParameterProjection implements IParameter {
    private IParameter original;
    private Map<ITypeVariableDefinition, ITypeReference> mappings;
    public ParameterProjection(IParameter original,
            Map<ITypeVariableDefinition, ITypeReference> mappings) {
        this.original = original;
        this.mappings = mappings;
    }
    public Set<IAnnotation> getAnnotations() {
        return original.getAnnotations();
    }
    public ITypeReference getType() {
        return ViewpointAdapter.substitutedTypeReference(original.getType(),
                mappings);
    }
    @Override
    public String toString() {
        return getType().toString();
    }
}
