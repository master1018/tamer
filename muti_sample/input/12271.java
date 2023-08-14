public class ByteToCharCp937 extends ByteToCharDBCS_EBCDIC {
    public String getCharacterEncoding() {
        return "Cp937";
    }
    public ByteToCharCp937() {
        super((DoubleByte.Decoder)new IBM937().newDecoder());
    }
}
