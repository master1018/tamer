public class ByteToCharEUC_CN extends ByteToCharDBCS_ASCII {
    private static DoubleByte.Decoder dec =
        (DoubleByte.Decoder)new EUC_CN().newDecoder();
    public String getCharacterEncoding() {
        return "EUC_CN";
    }
    public ByteToCharEUC_CN() {
        super(dec);
    }
}
