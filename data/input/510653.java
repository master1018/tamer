public abstract class SigTypeDefinitionDelta<T extends ITypeDefinition> extends
        SigDelta<T> implements ITypeDefinitionDelta<T> {
    public SigTypeDefinitionDelta(T from, T to) {
        super(from, to);
    }
}
