public class ByteToCharCp1141 extends ByteToCharCp273 {
    public ByteToCharCp1141() {}
    public String getCharacterEncoding() {
        return "Cp1141";
    }
    protected char getUnicode(int byteIndex) {
        return (byteIndex == (byte)0x9F) ? '\u20AC' : super.getUnicode(byteIndex);
    }
}
