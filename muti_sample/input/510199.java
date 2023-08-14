public abstract class ExecutableMemberProjection implements IExecutableMember {
    private final IExecutableMember original;
    private final Map<ITypeVariableDefinition, ITypeReference> mappings;
    public ExecutableMemberProjection(IExecutableMember original,
            Map<ITypeVariableDefinition, ITypeReference> mappings) {
        this.original = original;
        this.mappings = mappings;
    }
    public Set<IAnnotation> getAnnotations() {
        return original.getAnnotations();
    }
    public IClassDefinition getDeclaringClass() {
        throw new UnsupportedOperationException();
    }
    public Set<ITypeReference> getExceptions() {
        return ViewpointAdapter.substitutedTypeReferences(original
                .getExceptions(), mappings);
    }
    public Set<Modifier> getModifiers() {
        return original.getModifiers();
    }
    public String getName() {
        return original.getName();
    }
    public List<IParameter> getParameters() {
        List<IParameter> result = new LinkedList<IParameter>();
        for (IParameter parameter : original.getParameters()) {
            result.add(new ParameterProjection(parameter, mappings));
        }
        return result;
    }
    public List<ITypeVariableDefinition> getTypeParameters() {
        return original.getTypeParameters();
    }
}
