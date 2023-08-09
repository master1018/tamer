public class ByteToCharCp273 extends ByteToCharSingleByte {
    private final static IBM273 nioCoder = new IBM273();
    public String getCharacterEncoding() {
        return "Cp273";
    }
    public ByteToCharCp273() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
