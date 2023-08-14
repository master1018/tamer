public class CharToByteCp943 extends CharToByteDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp943";
    }
    public CharToByteCp943() {
        super((DoubleByte.Encoder)new IBM943().newEncoder());
    }
}
