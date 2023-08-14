public class ByteToCharCp037 extends ByteToCharSingleByte {
    private final static IBM037 nioCoder = new IBM037();
    public String getCharacterEncoding() {
        return "Cp037";
    }
    public ByteToCharCp037() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
