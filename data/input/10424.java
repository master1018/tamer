public class ByteToCharCp280 extends ByteToCharSingleByte {
    private final static IBM280 nioCoder = new IBM280();
    public String getCharacterEncoding() {
        return "Cp280";
    }
    public ByteToCharCp280() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
