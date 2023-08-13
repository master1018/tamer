public class ByteToCharTIS620 extends ByteToCharSingleByte {
    private final static TIS_620 nioCoder = new TIS_620();
    public String getCharacterEncoding() {
        return "TIS620";
    }
    public ByteToCharTIS620() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
