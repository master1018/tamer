public class CharToByteMS949 extends CharToByteDBCS_ASCII {
    private static DoubleByte.Encoder enc =
        (DoubleByte.Encoder)new MS949().newEncoder();
    public String getCharacterEncoding() {
        return "MS949";
    }
    public CharToByteMS949() {
        super(enc);
    }
}
