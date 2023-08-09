public class ByteToCharISO8859_9 extends ByteToCharSingleByte {
    private final static ISO_8859_9 nioCoder = new ISO_8859_9();
    public String getCharacterEncoding() {
        return "ISO8859_9";
    }
    public ByteToCharISO8859_9() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
