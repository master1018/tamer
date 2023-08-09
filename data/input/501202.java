public abstract class SigExecutableMember extends SigAnnotatableElement
        implements IExecutableMember, Serializable {
    private String name;
    private List<IParameter> parameters = Uninitialized.unset();
    private Set<ITypeReference> exceptions = Uninitialized.unset();
    private Set<Modifier> modifiers = Uninitialized.unset();
    private List<ITypeVariableDefinition> typeParameters = Uninitialized
            .unset();
    private IClassDefinition declaringClass = Uninitialized.unset();
    public SigExecutableMember(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public List<IParameter> getParameters() {
        return parameters;
    }
    public void setParameters(List<IParameter> parameters) {
        this.parameters = parameters;
    }
    public Set<ITypeReference> getExceptions() {
        return exceptions;
    }
    public void setExceptions(Set<ITypeReference> exceptions) {
        this.exceptions = exceptions;
    }
    public Set<Modifier> getModifiers() {
        return modifiers;
    }
    public void setModifiers(Set<Modifier> modifiers) {
        this.modifiers = modifiers;
    }
    public List<ITypeVariableDefinition> getTypeParameters() {
        return typeParameters;
    }
    public void setTypeParameters(
            List<ITypeVariableDefinition> typeParameters) {
        this.typeParameters = typeParameters;
    }
    public IClassDefinition getDeclaringClass() {
        return declaringClass;
    }
    public void setDeclaringClass(IClassDefinition declaringClass) {
        this.declaringClass = declaringClass;
    }
}
