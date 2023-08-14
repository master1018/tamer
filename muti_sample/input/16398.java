public class CharToByteMS932 extends CharToByteDBCS_ASCII {
    private static DoubleByte.Encoder enc =
        (DoubleByte.Encoder)new MS932().newEncoder();
    public String getCharacterEncoding() {
        return "MS932";
    }
    public CharToByteMS932() {
        super(enc);
    }
}
