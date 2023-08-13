public class ByteToCharCp1122 extends ByteToCharSingleByte {
    private final static IBM1122 nioCoder = new IBM1122();
    public String getCharacterEncoding() {
        return "Cp1122";
    }
    public ByteToCharCp1122() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
