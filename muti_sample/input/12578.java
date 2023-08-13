public class Literal extends Expression {
    private Object value;
    public Literal(Object value) {
        super();
        this.value = value;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
    public String toString() {
        return value.toString();
    }
}
