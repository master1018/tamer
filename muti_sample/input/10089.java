public class ByteToCharCp1112 extends ByteToCharSingleByte {
    private final static IBM1112 nioCoder = new IBM1112();
    public String getCharacterEncoding() {
        return "Cp1112";
    }
    public ByteToCharCp1112() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
