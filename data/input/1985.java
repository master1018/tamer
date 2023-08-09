public class ByteToCharCp949 extends ByteToCharDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp949";
    }
    public ByteToCharCp949() {
        super((DoubleByte.Decoder)new IBM949().newDecoder());
    }
}
