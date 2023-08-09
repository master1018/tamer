public class ByteToCharCp437 extends ByteToCharSingleByte {
    private final static IBM437 nioCoder = new IBM437();
    public String getCharacterEncoding() {
        return "Cp437";
    }
    public ByteToCharCp437() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
