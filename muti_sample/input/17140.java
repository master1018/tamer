public class CharToByteJohab extends CharToByteDBCS_ASCII {
    private static DoubleByte.Encoder enc =
        (DoubleByte.Encoder)new Johab().newEncoder();
    public String getCharacterEncoding() {
        return "Johab";
    }
    public CharToByteJohab() {
        super(enc);
    }
}
