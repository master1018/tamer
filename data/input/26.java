public class ContextValueMessage {
    private ICtxIdentifier identifier;
    private Serializable value;
    public ICtxIdentifier getIdentifier() {
        return identifier;
    }
    public void setIdentifier(ICtxIdentifier identifier) {
        this.identifier = identifier;
    }
    public Serializable getValue() {
        return value;
    }
    public void setValue(Serializable value) {
        this.value = value;
    }
}
