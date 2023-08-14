public class ByteToCharCp1251 extends ByteToCharSingleByte {
    private final static MS1251 nioCoder = new MS1251();
    public String getCharacterEncoding() {
        return "Cp1251";
    }
    public ByteToCharCp1251() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
