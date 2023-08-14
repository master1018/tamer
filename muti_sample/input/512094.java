public class MissingFormatArgumentException extends IllegalFormatException {
    private static final long serialVersionUID = 19190115L;
    private String s;
    public MissingFormatArgumentException(String s) {
        if (null == s) {
            throw new NullPointerException();
        }
        this.s = s;
    }
    public String getFormatSpecifier() {
        return s;
    }
    @Override
    public String getMessage() {
        return Msg.getString("K0348", s);
    }
}
