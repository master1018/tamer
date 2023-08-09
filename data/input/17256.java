public class ByteToCharCp950 extends ByteToCharDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp950";
    }
    public ByteToCharCp950() {
        super((DoubleByte.Decoder)new IBM950().newDecoder());
    }
}
