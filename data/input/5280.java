public class CharToByteCp948 extends CharToByteDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp948";
    }
    public CharToByteCp948() {
        super((DoubleByte.Encoder)new IBM948().newEncoder());
    }
}
