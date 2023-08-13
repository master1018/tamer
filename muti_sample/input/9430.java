public class ByteToCharCp424 extends ByteToCharSingleByte {
    private final static IBM424 nioCoder = new IBM424();
    public String getCharacterEncoding() {
        return "Cp424";
    }
    public ByteToCharCp424() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
