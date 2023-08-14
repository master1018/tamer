public class ParameterizedTypeProjection implements IParameterizedType {
    private final IParameterizedType original;
    private final Map<ITypeVariableDefinition, ITypeReference> mappings;
    public ParameterizedTypeProjection(IParameterizedType original,
            Map<ITypeVariableDefinition, ITypeReference> mappings) {
        this.original = original;
        this.mappings = mappings;
    }
    public ITypeReference getOwnerType() {
        ITypeReference ownerType = original.getOwnerType();
        if (ownerType == null) {
            return null;
        }
        return ViewpointAdapter.substitutedTypeReference(ownerType, mappings);
    }
    private IClassReference rawType = null;
    public IClassReference getRawType() {
        if (rawType == null) {
            rawType = (IClassReference) ViewpointAdapter
                    .substitutedTypeReference(original.getRawType(),
                            ViewpointAdapter.createTypeMapping(this, original
                                    .getRawType().getClassDefinition()));
        }
        return rawType;
    }
    private List<ITypeReference> arguments = null;
    public List<ITypeReference> getTypeArguments() {
        if (arguments == null) {
            arguments = ViewpointAdapter.substitutedTypeReferences(original
                    .getTypeArguments(), mappings);
        }
        return arguments;
    }
    @Override
    public int hashCode() {
        return SigParameterizedType.hashCode(this);
    }
    @Override
    public boolean equals(Object obj) {
        return SigParameterizedType.equals(this, obj);
    }
    @Override
    public String toString() {
        return SigParameterizedType.toString(this);
    }
}
