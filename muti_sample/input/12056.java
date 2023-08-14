public class ByteToCharCp852 extends ByteToCharSingleByte {
    private final static IBM852 nioCoder = new IBM852();
    public String getCharacterEncoding() {
        return "Cp852";
    }
    public ByteToCharCp852() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
