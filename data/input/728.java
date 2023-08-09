public class ByteToCharCp1383 extends ByteToCharEUC2 {
    public String getCharacterEncoding() {
        return "Cp1383";
    }
    public ByteToCharCp1383() {
        super((DoubleByte.Decoder)new IBM1383().newDecoder());
    }
}
