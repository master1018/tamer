public class ByteToCharMacGreek extends ByteToCharSingleByte {
    private final static MacGreek nioCoder = new MacGreek();
    public String getCharacterEncoding() {
        return "MacGreek";
    }
    public ByteToCharMacGreek() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
