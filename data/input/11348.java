public class ByteToCharCp868 extends ByteToCharSingleByte {
    private final static IBM868 nioCoder = new IBM868();
    public String getCharacterEncoding() {
        return "Cp868";
    }
    public ByteToCharCp868() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
