public class ByteToCharCp942 extends ByteToCharDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp942";
    }
    public ByteToCharCp942() {
        super((DoubleByte.Decoder)new IBM942().newDecoder());
    }
}
