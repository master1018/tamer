public class ByteToCharCp856 extends ByteToCharSingleByte {
    private final static IBM856 nioCoder = new IBM856();
    public String getCharacterEncoding() {
        return "Cp856";
    }
    public ByteToCharCp856() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
