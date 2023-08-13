public class JavaBoolean extends JavaValue {
    boolean value;
    public JavaBoolean(boolean value) {
        this.value = value;
    }
    public String toString() {
        return "" + value;
    }
}
