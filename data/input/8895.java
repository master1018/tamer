public class ByteToCharMS932 extends ByteToCharDBCS_ASCII {
    private static DoubleByte.Decoder dec =
        (DoubleByte.Decoder)new MS932().newDecoder();
    public String getCharacterEncoding() {
        return "MS932";
    }
    public ByteToCharMS932() {
        super(dec);
    }
}
