public class JavaFloat extends JavaValue {
    float value;
    public JavaFloat(float value) {
        this.value = value;
    }
    public String toString() {
        return Float.toString(value);
    }
}
