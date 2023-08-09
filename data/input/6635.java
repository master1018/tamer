public class ByteToCharCp850 extends ByteToCharSingleByte {
    public String getCharacterEncoding() {
        return "Cp850";
    }
    public ByteToCharCp850() {
        super.byteToCharTable = new IBM850().getDecoderSingleByteMappings();
    }
}
