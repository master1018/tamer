public class StringElementHandler extends ElementHandler {
    private StringBuilder sb = new StringBuilder();
    private ValueObject value = ValueObjectImpl.NULL;
    @Override
    public final void addCharacter(char ch) {
        if (this.sb == null) {
            throw new IllegalStateException("Could not add chararcter to evaluated string element");
        }
        this.sb.append(ch);
    }
    @Override
    protected final void addArgument(Object argument) {
        if (this.sb == null) {
            throw new IllegalStateException("Could not add argument to evaluated string element");
        }
        this.sb.append(argument);
    }
    @Override
    protected final ValueObject getValueObject() {
        if (this.sb != null) {
            try {
                this.value = ValueObjectImpl.create(getValue(this.sb.toString()));
            }
            catch (RuntimeException exception) {
                getOwner().handleException(exception);
            }
            finally {
                this.sb = null;
            }
        }
        return this.value;
    }
    protected Object getValue(String argument) {
        return argument;
    }
}
