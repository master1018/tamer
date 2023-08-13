public class SigTypeVariableReferenceDelta extends
        SigDelta<ITypeVariableReference> implements
        ITypeReferenceDelta<ITypeVariableReference> {
    private IGenericDeclarationDelta genericDeclarationDelta;
    public SigTypeVariableReferenceDelta(ITypeVariableReference from,
            ITypeVariableReference to) {
        super(from, to);
    }
    public IGenericDeclarationDelta getGenericDeclarationDelta() {
        return genericDeclarationDelta;
    }
    public void setGenericDeclarationDelta(
            IGenericDeclarationDelta genericDeclarationDelta) {
        this.genericDeclarationDelta = genericDeclarationDelta;
    }
}
