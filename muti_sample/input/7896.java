public class CharToByteCp949 extends CharToByteDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp949";
    }
    public CharToByteCp949() {
        super((DoubleByte.Encoder)new IBM949().newEncoder());
    }
}
