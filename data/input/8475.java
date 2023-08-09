public class ByteToCharCp866 extends ByteToCharSingleByte {
    private final static IBM866 nioCoder = new IBM866();
    public String getCharacterEncoding() {
        return "Cp866";
    }
    public ByteToCharCp866() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
