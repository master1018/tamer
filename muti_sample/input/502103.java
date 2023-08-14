public class IllegalFormatCodePointException extends IllegalFormatException
        implements Serializable {
    private static final long serialVersionUID = 19080630L;
    private int c;
    public IllegalFormatCodePointException(int c) {
        this.c = c;
    }
    public int getCodePoint() {
        return c;
    }
    @Override
    public String getMessage() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Code point is ");
        char[] chars = Character.toChars(c);
        for (int i = 0; i < chars.length; i++) {
            buffer.append(chars[i]);
        }
        return buffer.toString();
    }
}
