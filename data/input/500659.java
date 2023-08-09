public class StringIndexOutOfBoundsException extends IndexOutOfBoundsException {
    private static final long serialVersionUID = -6762910422159637258L;
    public StringIndexOutOfBoundsException() {
        super();
    }
    public StringIndexOutOfBoundsException(int index) {
        super(Msg.getString("K0055", index)); 
    }
    public StringIndexOutOfBoundsException(String detailMessage) {
        super(detailMessage);
    }
}
