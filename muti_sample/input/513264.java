public class IllegalFormatWidthException extends IllegalFormatException {
    private static final long serialVersionUID = 16660902L;
    private int w;
    public IllegalFormatWidthException(int w) {
        this.w = w;
    }
    public int getWidth() {
        return w;
    }
    @Override
    public String getMessage() {
        return String.valueOf(w);
    }
}
