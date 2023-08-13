public class ByteToCharISO8859_6 extends ByteToCharSingleByte {
    private final static ISO_8859_6 nioCoder = new ISO_8859_6();
    public String getCharacterEncoding() {
        return "ISO8859_6";
    }
    public ByteToCharISO8859_6() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
