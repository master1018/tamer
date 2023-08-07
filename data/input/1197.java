public class FatalLoaderException extends RuntimeException {
    private static final long serialVersionUID = 6208549987179699407L;
    public FatalLoaderException(String message) {
        super(message);
    }
    public FatalLoaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
