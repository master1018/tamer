public class CharToByteMS950 extends CharToByteDBCS_ASCII {
    private static DoubleByte.Encoder enc =
        (DoubleByte.Encoder)new MS950().newEncoder();
    public String getCharacterEncoding() {
        return "MS950";
    }
    public CharToByteMS950() {
        super(enc);
    }
}
