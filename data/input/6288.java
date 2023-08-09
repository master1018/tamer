public class HackJavaValue extends JavaValue {
    private String value;
    private int size;
    public HackJavaValue(String value, int size) {
        this.value = value;
        this.size = size;
    }
    public String toString() {
        return value;
    }
    public int getSize() {
        return size;
    }
}
