public class ByteToCharMacRoman extends ByteToCharSingleByte {
    private final static MacRoman nioCoder = new MacRoman();
    public String getCharacterEncoding() {
        return "MacRoman";
    }
    public ByteToCharMacRoman() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
