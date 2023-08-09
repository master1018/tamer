public class UnsupportedCallbackException extends Exception {
    private static final long serialVersionUID = -6873556327655666839L;
    private Callback callback;
    public UnsupportedCallbackException(Callback callback) {
        super();
        this.callback = callback;
    }
    public UnsupportedCallbackException(Callback callback, String message) {
        super(message);
        this.callback = callback;
    }
    public Callback getCallback() {
        return callback;
    }
}
