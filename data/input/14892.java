public class ByteToCharCp834 extends ByteToCharDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp834";
    }
    public ByteToCharCp834() {
        super((DoubleByte.Decoder)new IBM834().newDecoder());
    }
}
