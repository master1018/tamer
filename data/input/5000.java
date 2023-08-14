public class ByteToCharCp942C extends ByteToCharDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp942C";
    }
    public ByteToCharCp942C() {
        super((DoubleByte.Decoder)new IBM942C().newDecoder());
    }
}
