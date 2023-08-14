public class ByteToCharISO8859_15 extends ByteToCharSingleByte {
    private final static ISO_8859_15 nioCoder = new ISO_8859_15();
    public String getCharacterEncoding() {
        return "ISO8859_15";
    }
    public ByteToCharISO8859_15() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
