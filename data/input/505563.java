public class AssertionErrorWrapper extends RuntimeException {
    private static final long serialVersionUID = -2087349195182278608L;
    private final AssertionError error;
    public AssertionErrorWrapper(AssertionError error) {
        this.error = error;
    }
    public AssertionError getAssertionError() {
        return error;
    }
}
