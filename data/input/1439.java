public class ByteToCharCp861 extends ByteToCharSingleByte {
    private final static IBM861 nioCoder = new IBM861();
    public String getCharacterEncoding() {
        return "Cp861";
    }
    public ByteToCharCp861() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
