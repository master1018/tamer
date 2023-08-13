public class ByteToCharISO8859_5 extends ByteToCharSingleByte {
    private final static ISO_8859_5 nioCoder = new ISO_8859_5();
    public String getCharacterEncoding() {
        return "ISO8859_5";
    }
    public ByteToCharISO8859_5() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
