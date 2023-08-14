public class ByteToCharCp949C extends ByteToCharDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp949C";
    }
    public ByteToCharCp949C() {
        super((DoubleByte.Decoder)new IBM949C().newDecoder());
    }
}
