public class ByteToCharCp1250 extends ByteToCharSingleByte {
    private final static MS1250 nioCoder = new MS1250();
    public String getCharacterEncoding() {
        return "Cp1250";
    }
    public ByteToCharCp1250() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
