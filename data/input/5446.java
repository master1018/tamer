public class ByteToCharISO8859_3 extends ByteToCharSingleByte {
    private final static ISO_8859_3 nioCoder = new ISO_8859_3();
    public String getCharacterEncoding() {
        return "ISO8859_3";
    }
    public ByteToCharISO8859_3() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
