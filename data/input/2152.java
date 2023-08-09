public class ByteToCharCp943C extends ByteToCharDBCS_ASCII {
    public String getCharacterEncoding() {
        return "Cp943C";
    }
    public ByteToCharCp943C() {
        super((DoubleByte.Decoder)new IBM943C().newDecoder());
    }
}
