public class ByteToCharCp277 extends ByteToCharSingleByte {
    private final static IBM277 nioCoder = new IBM277();
    public String getCharacterEncoding() {
        return "Cp277";
    }
    public ByteToCharCp277() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
