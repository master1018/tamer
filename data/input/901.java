public class CharToByteCp942  extends CharToByteDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp942";
    }
    public CharToByteCp942() {
        super((DoubleByte.Encoder)new IBM942().newEncoder());
    }
}
