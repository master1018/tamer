public class CharToByteCp943C extends CharToByteDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp943C";
    }
    public CharToByteCp943C() {
        super((DoubleByte.Encoder)new IBM943C().newEncoder());
    }
}
