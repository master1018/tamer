public class InterruptedIOException extends IOException {
    private static final long serialVersionUID = 4020568460727500567L;
    public int bytesTransferred;
    public InterruptedIOException() {
        super();
    }
    public InterruptedIOException(String detailMessage) {
        super(detailMessage);
    }
}
