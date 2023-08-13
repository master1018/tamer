public class Same implements IArgumentMatcher, Serializable {
    private static final long serialVersionUID = 1094930851962278376L;
    private final Object expected;
    public Same(Object expected) {
        this.expected = expected;
    }
    public boolean matches(Object actual) {
        return expected == actual;
    }
    public void appendTo(StringBuffer buffer) {
        buffer.append("same(");
        ArgumentToString.appendArgument(expected, buffer);
        buffer.append(")");
    }
}
