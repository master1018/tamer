public class ByteToCharCp863 extends ByteToCharSingleByte {
    private final static IBM863 nioCoder = new IBM863();
    public String getCharacterEncoding() {
        return "Cp863";
    }
    public ByteToCharCp863() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
