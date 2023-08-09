public class ByteToCharBig5 extends ByteToCharDBCS_ASCII {
    private static DoubleByte.Decoder dec =
        (DoubleByte.Decoder)new Big5().newDecoder();
    public String getCharacterEncoding() {
        return "Big5";
    }
    public ByteToCharBig5() {
        super(dec);
    }
}
