public abstract class AbstractMatcher implements ArgumentsMatcher, Serializable {
    private static final long serialVersionUID = -5463061331694985383L;
    protected boolean argumentMatches(Object expected, Object actual) {
        return expected.equals(actual);
    }
    protected String argumentToString(Object argument) {
        StringBuffer result = new StringBuffer();
        ArgumentToString.appendArgument(argument, result);
        return result.toString();
    }
    public boolean matches(Object[] expected, Object[] actual) {
        if (expected == actual) {
            return true;
        }
        if (expected == null || actual == null) {
            return false;
        }
        if (expected.length != actual.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            Object expectedObject = expected[i];
            Object actualObject = actual[i];
            if (expectedObject == null && actualObject == null) {
                continue;
            }
            if (expectedObject == null && actualObject != null) {
                return false;
            }
            if (expectedObject != null && actualObject == null) {
                return false;
            }
            if (!argumentMatches(expectedObject, actualObject)) {
                return false;
            }
        }
        return true;
    }
    public String toString(Object[] arguments) {
        if (arguments == null)
            arguments = new Object[0];
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < arguments.length; i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(argumentToString(arguments[i]));
        }
        return result.toString();
    }
}
