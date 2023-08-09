public class LinkLoopException extends LinkException {
    public LinkLoopException(String explanation) {
        super(explanation);
    }
    public LinkLoopException() {
        super();
    }
    private static final long serialVersionUID = -3119189944325198009L;
}
