public class FailedLoginException extends LoginException {
    private static final long serialVersionUID = 802556922354616286L;
    public FailedLoginException() {
        super();
    }
    public FailedLoginException(String msg) {
        super(msg);
    }
}
