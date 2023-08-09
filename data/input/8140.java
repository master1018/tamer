public class ByteToCharCp775 extends ByteToCharSingleByte {
    private final static IBM775 nioCoder = new IBM775();
    public String getCharacterEncoding() {
        return "Cp775";
    }
    public ByteToCharCp775() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
