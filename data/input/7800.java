public class ByteToCharMS874 extends ByteToCharSingleByte {
    private final static MS874 nioCoder = new MS874();
    public String getCharacterEncoding() {
        return "MS874";
    }
    public ByteToCharMS874() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
