public class SigTypeVariableReference implements ITypeVariableReference,
        Serializable {
    private ITypeVariableDefinition definition;
    public SigTypeVariableReference(ITypeVariableDefinition definition) {
        this.definition = definition;
    }
    public ITypeVariableDefinition getTypeVariableDefinition() {
        return definition;
    }
    @Override
    public boolean equals(Object obj) {
        if (getClass() == obj.getClass()) {
            return getTypeVariableDefinition().equals(
                    ((ITypeVariableReference) obj).getTypeVariableDefinition());
        }
        return false;
    }
    @Override
    public int hashCode() {
        return getTypeVariableDefinition().hashCode();
    }
    @Override
    public String toString() {
        return getTypeVariableDefinition().getName();
    }
}
