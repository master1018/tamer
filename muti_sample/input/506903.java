public class UnsupportedCharsetException extends IllegalArgumentException {
    private static final long serialVersionUID = 1490765524727386367L;
    private String charsetName;
    public UnsupportedCharsetException(String charset) {
        super(Messages.getString("niochar.04", charset)); 
        this.charsetName = charset;
    }
    public String getCharsetName() {
        return this.charsetName;
    }
}
