public class CharToByteCp933 extends CharToByteDBCS_EBCDIC {
    public String getCharacterEncoding() {
        return "Cp933";
    }
    public CharToByteCp933() {
        super((DoubleByte.Encoder)new IBM933().newEncoder());
    }
}
