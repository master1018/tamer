public class ConstructorProjection extends ExecutableMemberProjection implements
        IConstructor {
    public ConstructorProjection(IConstructor original,
            Map<ITypeVariableDefinition, ITypeReference> mappings) {
        super(original, mappings);
    }
}
