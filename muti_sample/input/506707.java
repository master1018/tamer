public class MethodProjection extends ExecutableMemberProjection implements
        IMethod {
    private final IMethod original;
    private Map<ITypeVariableDefinition, ITypeReference> mappings;
    public MethodProjection(IMethod original,
            Map<ITypeVariableDefinition, ITypeReference> mappings) {
        super(original, mappings);
        this.mappings = mappings;
        this.original = original;
    }
    public ITypeReference getReturnType() {
        return ViewpointAdapter.substitutedTypeReference(original
                .getReturnType(), mappings);
    }
    @Override
    public String toString() {
        return "(" + SigMethod.toString(this) + " : " + mappings + " )";
    }
}
