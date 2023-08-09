public class WildcardTypeProjection implements IWildcardType {
    private final IWildcardType original;
    private final Map<ITypeVariableDefinition, ITypeReference> mappings;
    public WildcardTypeProjection(IWildcardType original,
            Map<ITypeVariableDefinition, ITypeReference> mappings) {
        this.original = original;
        this.mappings = mappings;
    }
    public ITypeReference getLowerBound() {
        return ViewpointAdapter.substitutedTypeReference(original
                .getLowerBound(), mappings);
    }
    public List<ITypeReference> getUpperBounds() {
        return ViewpointAdapter.substitutedTypeReferences(original
                .getUpperBounds(), mappings);
    }
    @Override
    public int hashCode() {
        return SigWildcardType.hashCode(this);
    }
    @Override
    public boolean equals(Object obj) {
        return SigWildcardType.equals(this, obj);
    }
    @Override
    public String toString() {
        return SigWildcardType.toString(this);
    }
}
