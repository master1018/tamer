public class ByteToCharMacCroatian extends ByteToCharSingleByte {
    private final static MacCroatian nioCoder = new MacCroatian();
    public String getCharacterEncoding() {
        return "MacCroatian";
    }
    public ByteToCharMacCroatian() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
