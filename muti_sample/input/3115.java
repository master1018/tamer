public class ByteToCharCp1025 extends ByteToCharSingleByte {
    private final static IBM1025 nioCoder = new IBM1025();
    public String getCharacterEncoding() {
        return "Cp1025";
    }
    public ByteToCharCp1025() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
