public class JavaDouble extends JavaValue {
    double value;
    public JavaDouble(double value) {
        this.value = value;
    }
    public String toString() {
        return Double.toString(value);
    }
}
