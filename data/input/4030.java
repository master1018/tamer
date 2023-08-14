public class ByteToCharCp1253 extends ByteToCharSingleByte {
    private final static MS1253 nioCoder = new MS1253();
    public String getCharacterEncoding() {
        return "Cp1253";
    }
    public ByteToCharCp1253() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
