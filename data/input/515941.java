public class Null implements IArgumentMatcher, Serializable {
    private static final long serialVersionUID = 6077244839421122011L;
    public static final Null NULL = new Null();
    private Null() {
    }
    public boolean matches(Object actual) {
        return actual == null;
    }
    public void appendTo(StringBuffer buffer) {
        buffer.append("isNull()");
    }
}
