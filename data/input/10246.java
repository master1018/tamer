public class CharToByteCp950 extends CharToByteDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp950";
    }
    public CharToByteCp950() {
        super((DoubleByte.Encoder)new IBM950().newEncoder());
    }
}
