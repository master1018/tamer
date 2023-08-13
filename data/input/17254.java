public class ByteToCharCp1046 extends ByteToCharSingleByte {
    private final static IBM1046 nioCoder = new IBM1046();
    public String getCharacterEncoding() {
        return "Cp1046";
    }
    public ByteToCharCp1046() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
