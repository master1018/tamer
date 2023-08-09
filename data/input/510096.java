public class ArrayMatcher extends AbstractMatcher {
    private static final long serialVersionUID = -4594659581004800814L;
    @Override
    public boolean argumentMatches(Object expected, Object actual) {
        return new ArrayEquals(expected).matches(actual);
    }
}