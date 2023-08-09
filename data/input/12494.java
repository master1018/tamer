public class ByteToCharCp833 extends ByteToCharSingleByte {
    private final static IBM833 nioCoder = new IBM833();
    public String getCharacterEncoding() {
        return "Cp833";
    }
    public ByteToCharCp833() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
