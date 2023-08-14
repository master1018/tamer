public class OptionalDataException extends ObjectStreamException {
    private static final long serialVersionUID = -8011121865681257820L;
    public boolean eof;
    public int length;
    OptionalDataException() {
        super();
    }
    OptionalDataException(String detailMessage) {
        super(detailMessage);
    }
}
