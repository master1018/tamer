public class StreamCorruptedException extends ObjectStreamException {
    private static final long serialVersionUID = 8983558202217591746L;
    public StreamCorruptedException() {
        super();
    }
    public StreamCorruptedException(String detailMessage) {
        super(detailMessage);
    }
}
