public class Identifier extends Expression {
    private String name;
    private Object value;
    public Identifier(String name) {
        super();
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setValue(Object value) {
        this.value = value;
    }
    public Object getValue() {
        return value;
    }
    public boolean isResolved() {
        return value != null;
    }
    public String toString() {
        return name;
    }
}
