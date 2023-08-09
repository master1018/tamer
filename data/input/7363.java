public class ByteToCharCp1255 extends ByteToCharSingleByte {
    private final static MS1255 nioCoder = new MS1255();
    public String getCharacterEncoding() {
        return "Cp1255";
    }
    public ByteToCharCp1255() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
