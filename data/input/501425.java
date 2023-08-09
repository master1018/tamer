public class SigParameterizedTypeDelta extends SigTypeDelta<IParameterizedType>
        implements IParameterizedTypeDelta {
    private ITypeReferenceDelta<?> ownerTypeDelta;
    private ITypeReferenceDelta<?> rawTypeDelta;
    private Set<ITypeReferenceDelta<?>> argumentTypeDeltas;
    public SigParameterizedTypeDelta(IParameterizedType from,
            IParameterizedType to) {
        super(from, to);
    }
    public ITypeReferenceDelta<?> getOwnerTypeDelta() {
        return ownerTypeDelta;
    }
    public void setOwnerTypeDelta(ITypeReferenceDelta<?> ownerTypeDelta) {
        this.ownerTypeDelta = ownerTypeDelta;
    }
    public ITypeReferenceDelta<?> getRawTypeDelta() {
        return rawTypeDelta;
    }
    public void setRawTypeDelta(ITypeReferenceDelta<?> rawTypeDelta) {
        this.rawTypeDelta = rawTypeDelta;
    }
    public Set<ITypeReferenceDelta<?>> getArgumentTypeDeltas() {
        return argumentTypeDeltas;
    }
    public void setArgumentTypeDeltas(
            Set<ITypeReferenceDelta<?>> argumentTypeDeltas) {
        this.argumentTypeDeltas = argumentTypeDeltas;
    }
}
