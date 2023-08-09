public class ByteToCharCp918 extends ByteToCharSingleByte {
    private final static IBM918 nioCoder = new IBM918();
    public String getCharacterEncoding() {
        return "Cp918";
    }
    public ByteToCharCp918() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
