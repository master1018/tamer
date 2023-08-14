public class ByteToCharMS949 extends ByteToCharDBCS_ASCII {
    private static DoubleByte.Decoder dec =
        (DoubleByte.Decoder)new MS949().newDecoder();
    public String getCharacterEncoding() {
        return "MS949";
    }
    public ByteToCharMS949() {
        super(dec);
    }
}
