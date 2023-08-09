public class ByteToCharMacHebrew extends ByteToCharSingleByte {
    private final static MacHebrew nioCoder = new MacHebrew();
    public String getCharacterEncoding() {
        return "MacHebrew";
    }
    public ByteToCharMacHebrew() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
