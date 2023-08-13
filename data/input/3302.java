public class ByteToCharMacThai extends ByteToCharSingleByte {
    private final static MacThai nioCoder = new MacThai();
    public String getCharacterEncoding() {
        return "MacThai";
    }
    public ByteToCharMacThai() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
