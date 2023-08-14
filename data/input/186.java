public class CharToByteCp939 extends CharToByteDBCS_EBCDIC {
    public String getCharacterEncoding() {
        return "Cp939";
    }
    public CharToByteCp939() {
        super((DoubleByte.Encoder)new IBM939().newEncoder());
    }
}
