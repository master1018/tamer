public class ByteToCharCp970 extends ByteToCharEUC2 {
    public String getCharacterEncoding() {
        return "Cp970";
    }
    public ByteToCharCp970() {
        super((DoubleByte.Decoder)new IBM970().newDecoder());
    }
}
