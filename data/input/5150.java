public class FieldInfo {
    private String name;
    private String signature;
    private int    modifiers;
    private int    slot;
    FieldInfo() {
    }
    public String name() {
        return name;
    }
    public String signature() {
        return signature;
    }
    public int modifiers() {
        return modifiers;
    }
    public int slot() {
        return slot;
    }
    public boolean isPublic() {
        return (Modifier.isPublic(modifiers()));
    }
}
