public class ByteToCharCp1381 extends ByteToCharDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp1381";
    }
    public ByteToCharCp1381() {
        super((DoubleByte.Decoder)new IBM1381().newDecoder());
    }
}
