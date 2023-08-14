public class ByteToCharKOI8_R extends ByteToCharSingleByte {
    private final static KOI8_R nioCoder = new KOI8_R();
    public String getCharacterEncoding() {
        return "KOI8_R";
    }
    public ByteToCharKOI8_R() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
