public class ByteToCharMacSymbol extends ByteToCharSingleByte {
    private final static MacSymbol nioCoder = new MacSymbol();
    public String getCharacterEncoding() {
        return "MacSymbol";
    }
    public ByteToCharMacSymbol() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
