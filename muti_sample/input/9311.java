public class ProviderException extends RuntimeException {
    private static final long serialVersionUID = 5256023526693665674L;
    public ProviderException() {
        super();
    }
    public ProviderException(String s) {
        super(s);
    }
    public ProviderException(String message, Throwable cause) {
        super(message, cause);
    }
    public ProviderException(Throwable cause) {
        super(cause);
    }
}
