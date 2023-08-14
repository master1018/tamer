public class ByteToCharMacCentralEurope extends ByteToCharSingleByte {
    private final static MacCentralEurope nioCoder = new MacCentralEurope();
    public String getCharacterEncoding() {
        return "MacCentralEurope";
    }
    public ByteToCharMacCentralEurope() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
