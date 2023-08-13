public class CharToByteCp935 extends CharToByteDBCS_EBCDIC {
    public String getCharacterEncoding() {
        return "Cp935";
    }
    public CharToByteCp935() {
        super((DoubleByte.Encoder)new IBM935().newEncoder());
    }
}
