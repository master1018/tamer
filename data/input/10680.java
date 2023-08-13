public class ByteToCharCp1256 extends ByteToCharSingleByte {
    private final static MS1256 nioCoder = new MS1256();
    public String getCharacterEncoding() {
        return "Cp1256";
    }
    public ByteToCharCp1256() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
