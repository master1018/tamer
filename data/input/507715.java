public class ArrayIndexOutOfBoundsException extends IndexOutOfBoundsException {
    private static final long serialVersionUID = -5116101128118950844L;
    public ArrayIndexOutOfBoundsException() {
        super();
    }
    public ArrayIndexOutOfBoundsException(int index) {
        super(Msg.getString("K0052", index)); 
    }
    public ArrayIndexOutOfBoundsException(String detailMessage) {
        super(detailMessage);
    }
}
