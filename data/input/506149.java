public abstract class ObjectStreamException extends IOException {
    private static final long serialVersionUID = 7260898174833392607L;
    protected ObjectStreamException() {
        super();
    }
    protected ObjectStreamException(String detailMessage) {
        super(detailMessage);
    }
}
