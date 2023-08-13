public class ByteToCharCp939 extends ByteToCharDBCS_EBCDIC {
    public String getCharacterEncoding() {
        return "Cp939";
    }
    public ByteToCharCp939() {
        super((DoubleByte.Decoder)new IBM939().newDecoder());
    }
}
