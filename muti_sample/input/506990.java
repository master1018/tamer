public class SigArrayTypeDelta extends SigTypeDelta<IArrayType> implements
        IArrayTypeDelta {
    private ITypeReferenceDelta<?> componentTypeDelta;
    public SigArrayTypeDelta(IArrayType from, IArrayType to) {
        super(from, to);
    }
    public ITypeReferenceDelta<?> getComponentTypeDelta() {
        return componentTypeDelta;
    }
    public void setComponentTypeDelta(
            ITypeReferenceDelta<?> componentTypeDelta) {
        this.componentTypeDelta = componentTypeDelta;
    }
}
