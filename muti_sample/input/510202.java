public class ArrayTypeProjection implements IArrayType {
    private final IArrayType original;
    private final Map<ITypeVariableDefinition, ITypeReference> mappings;
    public ArrayTypeProjection(IArrayType original,
            Map<ITypeVariableDefinition, ITypeReference> mappings) {
        this.original = original;
        this.mappings = mappings;
    }
    public ITypeReference getComponentType() {
        return ViewpointAdapter.substitutedTypeReference(original
                .getComponentType(), mappings);
    }
    @Override
    public int hashCode() {
        return SigArrayType.hashCode(this);
    }
    @Override
    public boolean equals(Object obj) {
        return SigArrayType.equals(this, obj);
    }
    @Override
    public String toString() {
        return "(" + SigArrayType.toString(this) + " : " + mappings + " )";
    }
}
