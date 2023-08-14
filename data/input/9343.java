public class ByteToCharMacDingbat extends ByteToCharSingleByte {
    private final static MacDingbat nioCoder = new MacDingbat();
    public String getCharacterEncoding() {
        return "MacDingbat";
    }
    public ByteToCharMacDingbat() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
