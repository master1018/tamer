public class ByteToCharCp285 extends ByteToCharSingleByte {
    private final static IBM285 nioCoder = new IBM285();
    public String getCharacterEncoding() {
        return "Cp285";
    }
    public ByteToCharCp285() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
