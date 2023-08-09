public class SigTypeDelta<T extends ITypeReference> extends SigDelta<T>
        implements ITypeReferenceDelta<T> {
    public SigTypeDelta(T from, T to) {
        super(from, to);
    }
}
