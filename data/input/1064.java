public class ByteToCharCp857 extends ByteToCharSingleByte {
    private final static IBM857 nioCoder = new IBM857();
    public String getCharacterEncoding() {
        return "Cp857";
    }
    public ByteToCharCp857() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
