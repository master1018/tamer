public class CharToByteBig5_Solaris extends CharToByteDBCS_ASCII {
    private static DoubleByte.Encoder enc =
        (DoubleByte.Encoder)new Big5_Solaris().newEncoder();
    public String getCharacterEncoding() {
        return "Big5_Solaris";
    }
    public CharToByteBig5_Solaris() {
        super(enc);
    }
}
