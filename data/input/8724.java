public class JavaLong extends JavaValue {
    long value;
    public JavaLong(long value) {
        this.value = value;
    }
    public String toString() {
        return Long.toString(value);
    }
}
