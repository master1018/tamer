public class LocaleSyntaxException extends Exception {
    private static final long serialVersionUID = 1L;
    private int index = -1;
    public LocaleSyntaxException(String msg) {
        this(msg, 0);
    }
    public LocaleSyntaxException(String msg, int errorIndex) {
        super(msg);
        index = errorIndex;
    }
    public int getErrorIndex() {
        return index;
    }
}
