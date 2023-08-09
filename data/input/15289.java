public class ByteToCharBig5_Solaris extends ByteToCharDBCS_ASCII {
    private static DoubleByte.Decoder dec =
        (DoubleByte.Decoder)new Big5_Solaris().newDecoder();
    public String getCharacterEncoding() {
        return "Big5_Solaris";
    }
    public ByteToCharBig5_Solaris() {
        super(dec);
    }
}
