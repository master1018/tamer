public class SigMethodDelta extends SigExecutableMemberDelta<IMethod> implements
        IMethodDelta {
    private ITypeReferenceDelta<?> returnTypeDelta;
    public SigMethodDelta(IMethod from, IMethod to) {
        super(from, to);
    }
    public ITypeReferenceDelta<?> getReturnTypeDelta() {
        return returnTypeDelta;
    }
    public void setReturnTypeDelta(ITypeReferenceDelta<?> returnTypeDelta) {
        this.returnTypeDelta = returnTypeDelta;
    }
}
