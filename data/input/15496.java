public class ByteToCharCp874 extends ByteToCharSingleByte {
    private final static IBM874 nioCoder = new IBM874();
    public String getCharacterEncoding() {
        return "Cp874";
    }
    public ByteToCharCp874() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
