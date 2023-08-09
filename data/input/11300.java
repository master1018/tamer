public class ByteToCharCp1257 extends ByteToCharSingleByte {
    private final static MS1257 nioCoder = new MS1257();
    public String getCharacterEncoding() {
        return "Cp1257";
    }
    public ByteToCharCp1257() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
