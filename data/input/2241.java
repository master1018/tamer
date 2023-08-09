public class ByteToCharISO8859_4 extends ByteToCharSingleByte {
    private final static ISO_8859_4 nioCoder = new ISO_8859_4();
    public String getCharacterEncoding() {
        return "ISO8859_4";
    }
    public ByteToCharISO8859_4() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
