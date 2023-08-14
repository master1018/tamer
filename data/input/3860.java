public class RIFFInvalidFormatException extends InvalidFormatException {
    private static final long serialVersionUID = 1L;
    public RIFFInvalidFormatException() {
        super("Invalid format!");
    }
    public RIFFInvalidFormatException(String s) {
        super(s);
    }
}
