public class CharToByteCp1383 extends CharToByteDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp1383";
    }
    public CharToByteCp1383() {
        super((DoubleByte.Encoder)new IBM1383().newEncoder());
    }
}
