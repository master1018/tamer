public class ByteToCharCp855 extends ByteToCharSingleByte {
    private final static IBM855 nioCoder = new IBM855();
    public String getCharacterEncoding() {
        return "Cp855";
    }
    public ByteToCharCp855() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
