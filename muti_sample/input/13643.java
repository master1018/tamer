final class JavaElementHandler extends ElementHandler {
    private Class<?> type;
    private ValueObject value;
    @Override
    public void addAttribute(String name, String value) {
        if (name.equals("version")) { 
        } else if (name.equals("class")) { 
            this.type = getOwner().findClass(value);
        } else {
            super.addAttribute(name, value);
        }
    }
    @Override
    protected void addArgument(Object argument) {
        getOwner().addObject(argument);
    }
    @Override
    protected boolean isArgument() {
        return false; 
    }
    @Override
    protected ValueObject getValueObject() {
        if (this.value == null) {
            this.value = ValueObjectImpl.create(getValue());
        }
        return this.value;
    }
    private Object getValue() {
        Object owner = getOwner().getOwner();
        if ((this.type == null) || isValid(owner)) {
            return owner;
        }
        if (owner instanceof XMLDecoder) {
            XMLDecoder decoder = (XMLDecoder) owner;
            owner = decoder.getOwner();
            if (isValid(owner)) {
                return owner;
            }
        }
        throw new IllegalStateException("Unexpected owner class: " + owner.getClass().getName());
    }
    private boolean isValid(Object owner) {
        return (owner == null) || this.type.isInstance(owner);
    }
}
