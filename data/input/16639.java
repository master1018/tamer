class NullElementHandler extends ElementHandler implements ValueObject {
    @Override
    protected final ValueObject getValueObject() {
        return this;
    }
    public Object getValue() {
        return null;
    }
    public final boolean isVoid() {
        return false;
    }
}
