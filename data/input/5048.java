public class ByteToCharMacTurkish extends ByteToCharSingleByte {
    private final static MacTurkish nioCoder = new MacTurkish();
    public String getCharacterEncoding() {
        return "MacTurkish";
    }
    public ByteToCharMacTurkish() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
