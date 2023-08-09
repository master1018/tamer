public class LSException extends RuntimeException {
    public LSException(short code, String message) {
       super(message);
       this.code = code;
    }
    public short   code;
    public static final short PARSE_ERR                 = 81;
    public static final short SERIALIZE_ERR             = 82;
}
