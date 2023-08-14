public class ByteToCharJohab extends ByteToCharDBCS_ASCII {
    private static DoubleByte.Decoder dec =
        (DoubleByte.Decoder)new Johab().newDecoder();
    public String getCharacterEncoding() {
        return "Johab";
    }
    public ByteToCharJohab() {
        super(dec);
    }
}
