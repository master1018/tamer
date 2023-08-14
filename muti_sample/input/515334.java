public class AlertException extends RuntimeException {
    private static final long serialVersionUID = -4448327177165687581L;
    private final SSLException reason;
    private final byte description;
    protected AlertException(byte description, SSLException reason) {
        super(reason);
        this.reason = reason;
        this.description = description;
    }
    protected SSLException getReason() {
        return reason;
    }
    protected byte getDescriptionCode() {
        return description;
    }
}
