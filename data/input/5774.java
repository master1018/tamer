public class ByteToCharCp1254 extends ByteToCharSingleByte {
    private final static MS1254 nioCoder = new MS1254();
    public String getCharacterEncoding() {
        return "Cp1254";
    }
    public ByteToCharCp1254() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
