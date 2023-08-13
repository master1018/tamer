public class ByteToCharCp869 extends ByteToCharSingleByte {
    private final static IBM869 nioCoder = new IBM869();
    public String getCharacterEncoding() {
        return "Cp869";
    }
    public ByteToCharCp869() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
