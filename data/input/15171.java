public class ByteToCharCp930 extends ByteToCharDBCS_EBCDIC {
    public String getCharacterEncoding() {
        return "Cp930";
    }
    public ByteToCharCp930() {
        super((DoubleByte.Decoder)new IBM930().newDecoder());
    }
}
