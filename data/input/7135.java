public class ByteToCharMS950 extends ByteToCharDBCS_ASCII {
    private static DoubleByte.Decoder dec =
        (DoubleByte.Decoder)new MS950().newDecoder();
    public String getCharacterEncoding() {
        return "MS950";
    }
    public ByteToCharMS950() {
        super(dec);
    }
}
