public abstract class BaseAttribute implements Attribute {
    private final String name;
    public BaseAttribute(String name) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
