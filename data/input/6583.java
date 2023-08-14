public class ByteToCharMacRomania extends ByteToCharSingleByte {
    private final static MacRomania nioCoder = new MacRomania();
    public String getCharacterEncoding() {
        return "MacRomania";
    }
    public ByteToCharMacRomania() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
