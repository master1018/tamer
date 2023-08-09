public class CharToByteBig5 extends CharToByteDBCS_ASCII {
    private static DoubleByte.Encoder enc =
        (DoubleByte.Encoder)new Big5().newEncoder();
    public String getCharacterEncoding() {
        return "Big5";
    }
    public CharToByteBig5() {
        super(enc);
    }
}
