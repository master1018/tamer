public abstract class BaseLocalVariables extends BaseAttribute {
    private final LocalVariableList localVariables;
    public BaseLocalVariables(String name,
            LocalVariableList localVariables) {
        super(name);
        try {
            if (localVariables.isMutable()) {
                throw new MutabilityException("localVariables.isMutable()");
            }
        } catch (NullPointerException ex) {
            throw new NullPointerException("localVariables == null");
        }
        this.localVariables = localVariables;
    }
    public final int byteLength() {
        return 8 + localVariables.size() * 10;
    }
    public final LocalVariableList getLocalVariables() {
        return localVariables;
    }
}
