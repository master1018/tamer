public class UnknownFormatConversionException extends IllegalFormatException {
    private static final long serialVersionUID = 19060418L;
    private String s;
    public UnknownFormatConversionException(String s) {
        this.s = s;
    }
    public String getConversion() {
        return s;
    }
    @Override
    public String getMessage() {
        return Msg.getString("K0349", s);
    }
}
