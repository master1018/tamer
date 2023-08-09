final class ValueObjectImpl implements ValueObject {
    static final ValueObject NULL = new ValueObjectImpl(null);
    static final ValueObject VOID = new ValueObjectImpl();
    static ValueObject create(Object value) {
        return (value != null)
                ? new ValueObjectImpl(value)
                : NULL;
    }
    private Object value;
    private boolean isVoid;
    private ValueObjectImpl() {
        this.isVoid = true;
    }
    private ValueObjectImpl(Object value) {
        this.value = value;
    }
    public Object getValue() {
        return this.value;
    }
    public boolean isVoid() {
        return this.isVoid;
    }
}
