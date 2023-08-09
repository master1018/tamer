public class ByteToCharISO8859_7 extends ByteToCharSingleByte {
    private final static ISO_8859_7 nioCoder = new ISO_8859_7();
    public String getCharacterEncoding() {
        return "ISO8859_7";
    }
    public ByteToCharISO8859_7() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
