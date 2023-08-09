public class ByteToCharCp933 extends ByteToCharDBCS_EBCDIC {
    public String getCharacterEncoding() {
        return "Cp933";
    }
    public ByteToCharCp933() {
        super((DoubleByte.Decoder)new IBM933().newDecoder());
    }
}
