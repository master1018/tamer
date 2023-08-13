public class ByteToCharMacIceland extends ByteToCharSingleByte {
    private final static MacIceland nioCoder = new MacIceland();
    public String getCharacterEncoding() {
        return "MacIceland";
    }
    public ByteToCharMacIceland() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
