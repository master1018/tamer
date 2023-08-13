public class ByteToCharCp278 extends ByteToCharSingleByte {
    private final static IBM278 nioCoder = new IBM278();
    public String getCharacterEncoding() {
        return "Cp278";
    }
    public ByteToCharCp278() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
