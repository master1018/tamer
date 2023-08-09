public class ByteToCharCp500 extends ByteToCharSingleByte {
    private final static IBM500 nioCoder = new IBM500();
    public String getCharacterEncoding() {
        return "Cp500";
    }
    public ByteToCharCp500() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
