public class ByteToCharCp943 extends ByteToCharDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp943";
    }
    public ByteToCharCp943() {
        super((DoubleByte.Decoder)new IBM943().newDecoder());
    }
}
