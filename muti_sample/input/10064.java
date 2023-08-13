public class NameAlreadyBoundException extends NamingException {
    public NameAlreadyBoundException(String explanation) {
        super(explanation);
    }
    public NameAlreadyBoundException() {
        super();
    }
    private static final long serialVersionUID = -8491441000356780586L;
}
