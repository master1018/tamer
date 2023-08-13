public class ByteToCharCp875 extends ByteToCharSingleByte {
    private final static IBM875 nioCoder = new IBM875();
    public String getCharacterEncoding() {
        return "Cp875";
    }
    public ByteToCharCp875() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
