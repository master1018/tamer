public class ByteToCharCp737 extends ByteToCharSingleByte {
    private final static IBM737 nioCoder = new IBM737();
    public String getCharacterEncoding() {
        return "Cp737";
    }
    public ByteToCharCp737() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
