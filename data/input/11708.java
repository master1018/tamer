final class VarElementHandler extends ElementHandler {
    private ValueObject value;
    @Override
    public void addAttribute(String name, String value) {
        if (name.equals("idref")) { 
            this.value = ValueObjectImpl.create(getVariable(value));
        } else {
            super.addAttribute(name, value);
        }
    }
    @Override
    protected ValueObject getValueObject() {
        if (this.value == null) {
            throw new IllegalArgumentException("Variable name is not set");
        }
        return this.value;
    }
}
