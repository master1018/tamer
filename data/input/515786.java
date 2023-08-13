public class SigTypeVariableDefinitionDelta extends
        SigDelta<ITypeVariableDefinition> implements
        ITypeVariableDefinitionDelta {
    private IUpperBoundsDelta upperBoundsDelta;
    private IGenericDeclarationDelta genericDeclarationDelta;
    public SigTypeVariableDefinitionDelta(ITypeVariableDefinition from,
            ITypeVariableDefinition to) {
        super(from, to);
    }
    public IUpperBoundsDelta getUpperBoundsDelta() {
        return upperBoundsDelta;
    }
    public void setUpperBoundsDelta(IUpperBoundsDelta upperBoundsDelta) {
        this.upperBoundsDelta = upperBoundsDelta;
    }
    public IGenericDeclarationDelta getGenericDeclarationDelta() {
        return genericDeclarationDelta;
    }
    public void setGenericDeclarationDelta(
            IGenericDeclarationDelta genericDeclarationDelta) {
        this.genericDeclarationDelta = genericDeclarationDelta;
    }
}
