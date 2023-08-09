public class ByteToCharCp935 extends ByteToCharDBCS_EBCDIC {
    public String getCharacterEncoding() {
        return "Cp935";
    }
    public ByteToCharCp935() {
        super((DoubleByte.Decoder)new IBM935().newDecoder());
    }
}
