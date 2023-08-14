public class ByteToCharMacCyrillic extends ByteToCharSingleByte {
    private final static MacCyrillic nioCoder = new MacCyrillic();
    public String getCharacterEncoding() {
        return "MacCyrillic";
    }
    public ByteToCharMacCyrillic() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
