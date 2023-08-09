public class ByteToCharCp297 extends ByteToCharSingleByte {
    private final static IBM297 nioCoder = new IBM297();
    public String getCharacterEncoding() {
        return "Cp297";
    }
    public ByteToCharCp297() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
