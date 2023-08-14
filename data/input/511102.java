public class AlwaysMatcher extends AbstractMatcher {
    private static final long serialVersionUID = 592339838132342008L;
    public boolean matches(Object[] expected, Object[] actual) {
        return true;
    }
    protected String argumentToString(Object argument) {
        return "<any>";
    }
}