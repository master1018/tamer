public class Or implements IArgumentMatcher, Serializable {
    private static final long serialVersionUID = -5701204283180444317L;
    private final List<IArgumentMatcher> matchers;
    public Or(List<IArgumentMatcher> matchers) {
        this.matchers = matchers;
    }
    public boolean matches(Object actual) {
        for (IArgumentMatcher matcher : matchers) {
            if (matcher.matches(actual)) {
                return true;
            }
        }
        return false;
    }
    public void appendTo(StringBuffer buffer) {
        buffer.append("or(");
        for (Iterator<IArgumentMatcher> it = matchers.iterator(); it.hasNext();) {
            it.next().appendTo(buffer);
            if (it.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append(")");
    }
}
