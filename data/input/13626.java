abstract class AccessorElementHandler extends ElementHandler {
    private String name;
    private ValueObject value;
    @Override
    public void addAttribute(String name, String value) {
        if (name.equals("name")) { 
            this.name = value;
        } else {
            super.addAttribute(name, value);
        }
    }
    @Override
    protected final void addArgument(Object argument) {
        if (this.value != null) {
            throw new IllegalStateException("Could not add argument to evaluated element");
        }
        setValue(this.name, argument);
        this.value = ValueObjectImpl.VOID;
    }
    @Override
    protected final ValueObject getValueObject() {
        if (this.value == null) {
            this.value = ValueObjectImpl.create(getValue(this.name));
        }
        return this.value;
    }
    protected abstract Object getValue(String name);
    protected abstract void setValue(String name, Object value);
}
