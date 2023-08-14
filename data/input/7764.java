public class CharToByteMS936 extends CharToByteDBCS_ASCII {
    private static DoubleByte.Encoder enc =
        (DoubleByte.Encoder)new MS936().newEncoder();
    public String getCharacterEncoding() {
        return "MS936";
    }
    public CharToByteMS936() {
        super(enc);
    }
}
