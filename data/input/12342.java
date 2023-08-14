public class ByteToCharCp864 extends ByteToCharSingleByte {
    private final static IBM864 nioCoder = new IBM864();
    public String getCharacterEncoding() {
        return "Cp864";
    }
    public ByteToCharCp864() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
