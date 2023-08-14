public class And implements IArgumentMatcher, Serializable {
    private static final long serialVersionUID = 3874580646798403818L;
    private final List<IArgumentMatcher> matchers;
    public And(List<IArgumentMatcher> matchers) {
        this.matchers = matchers;
    }
    public boolean matches(Object actual) {
        for (IArgumentMatcher matcher : matchers) {
            if (!matcher.matches(actual)) {
                return false;
            }
        }
        return true;
    }
    public void appendTo(StringBuffer buffer) {
        buffer.append("and(");
        for (Iterator<IArgumentMatcher> it = matchers.iterator(); it.hasNext();) {
            it.next().appendTo(buffer);
            if (it.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append(")");
    }
}
