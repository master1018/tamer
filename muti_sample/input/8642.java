public class ByteToCharCp870 extends ByteToCharSingleByte {
    private final static IBM870 nioCoder = new IBM870();
    public String getCharacterEncoding() {
        return "Cp870";
    }
    public ByteToCharCp870() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
