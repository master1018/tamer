public class ByteToCharCp1123 extends ByteToCharSingleByte {
    private final static IBM1123 nioCoder = new IBM1123();
    public String getCharacterEncoding() {
        return "Cp1123";
    }
    public ByteToCharCp1123() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
