public class ByteToCharEUC_KR extends ByteToCharDBCS_ASCII {
    private static DoubleByte.Decoder dec =
        (DoubleByte.Decoder)new EUC_KR().newDecoder();
    public String getCharacterEncoding() {
        return "EUC_KR";
    }
    public ByteToCharEUC_KR() {
        super(dec);
    }
}
