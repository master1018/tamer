public class ByteToCharCp1026 extends ByteToCharSingleByte {
    private final static IBM1026 nioCoder = new IBM1026();
    public String getCharacterEncoding() {
        return "Cp1026";
    }
    public ByteToCharCp1026() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
