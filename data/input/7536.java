public class ByteToCharGBK extends ByteToCharDBCS_ASCII {
    private static DoubleByte.Decoder dec =
        (DoubleByte.Decoder)new GBK().newDecoder();
    public String getCharacterEncoding() {
        return "GBK";
    }
    public ByteToCharGBK() {
        super(dec);
    }
}
