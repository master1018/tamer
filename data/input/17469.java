public class CharToByteCp942C extends CharToByteDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp942C";
    }
    public CharToByteCp942C() {
        super((DoubleByte.Encoder)new IBM942C().newEncoder());
    }
}
