public class ByteToCharCp1258 extends ByteToCharSingleByte {
    private final static MS1258 nioCoder = new MS1258();
    public String getCharacterEncoding() {
        return "Cp1258";
    }
    public ByteToCharCp1258() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
