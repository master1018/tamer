public class CharToByteCp937 extends CharToByteDBCS_EBCDIC {
    public String getCharacterEncoding() {
        return "Cp937";
    }
    public CharToByteCp937() {
        super((DoubleByte.Encoder)new IBM937().newEncoder());
    }
}
