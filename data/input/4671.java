public class ByteToCharCp922 extends ByteToCharSingleByte {
    private final static IBM922 nioCoder = new IBM922();
    public String getCharacterEncoding() {
        return "Cp922";
    }
    public ByteToCharCp922() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
