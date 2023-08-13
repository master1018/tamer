public class ByteToCharCp862 extends ByteToCharSingleByte {
    private final static IBM862 nioCoder = new IBM862();
    public String getCharacterEncoding() {
        return "Cp862";
    }
    public ByteToCharCp862() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
