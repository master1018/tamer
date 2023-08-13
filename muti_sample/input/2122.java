public class ByteToCharMS936 extends ByteToCharDBCS_ASCII {
    private static DoubleByte.Decoder dec =
        (DoubleByte.Decoder)new MS936().newDecoder();
    public String getCharacterEncoding() {
        return "MS936";
    }
    public ByteToCharMS936() {
        super(dec);
    }
}
