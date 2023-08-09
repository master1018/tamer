public class JMXServerErrorException extends IOException {
    private static final long serialVersionUID = 3996732239558744666L;
    public JMXServerErrorException(String s, Error err) {
        super(s);
        cause = err;
    }
    public Throwable getCause() {
        return cause;
    }
    private final Error cause;
}
