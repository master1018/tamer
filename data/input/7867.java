public class CharToByteCp949C extends CharToByteDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp949C";
    }
    public CharToByteCp949C() {
        super((DoubleByte.Encoder)new IBM949C().newEncoder());
    }
}
