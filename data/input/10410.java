public class ByteToCharMacUkraine extends ByteToCharSingleByte {
    private final static MacUkraine nioCoder = new MacUkraine();
    public String getCharacterEncoding() {
        return "MacUkraine";
    }
    public ByteToCharMacUkraine() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
