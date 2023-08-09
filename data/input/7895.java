public class RIFFInvalidDataException extends InvalidDataException {
    private static final long serialVersionUID = 1L;
    public RIFFInvalidDataException() {
        super("Invalid Data!");
    }
    public RIFFInvalidDataException(String s) {
        super(s);
    }
}
