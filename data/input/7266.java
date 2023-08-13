public class JavaField {
    private String name;
    private String signature;
    public JavaField(String name, String signature) {
        this.name = name;
        this.signature = signature;
    }
    public boolean hasId() {
        char ch = signature.charAt(0);
        return (ch == '[' || ch == 'L');
    }
    public String getName() {
        return name;
    }
    public String getSignature() {
        return signature;
    }
}
