public class ByteToCharISO8859_13 extends ByteToCharSingleByte {
    private final static ISO_8859_13 nioCoder = new ISO_8859_13();
    public String getCharacterEncoding() {
        return "ISO8859_13";
    }
    public ByteToCharISO8859_13() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
