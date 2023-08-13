public class ByteToCharCp948 extends ByteToCharDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp948";
    }
    public ByteToCharCp948() {
        super((DoubleByte.Decoder)new IBM948().newDecoder());
    }
}
