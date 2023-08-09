public class CharToByteBig5_HKSCS extends CharToByteDBCS_ASCII {
    private static DoubleByte.Encoder enc =
        (DoubleByte.Encoder)new Big5_HKSCS().newEncoder();
    public String getCharacterEncoding() {
        return "Big5_HKSCS";
    }
    public CharToByteBig5_HKSCS() {
        super(enc);
    }
}
