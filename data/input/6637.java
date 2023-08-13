public class ByteToCharCp1146 extends ByteToCharCp285 {
    public ByteToCharCp1146() {}
    public String getCharacterEncoding() {
        return "Cp1146";
    }
    protected char getUnicode(int byteIndex) {
        return (byteIndex == (byte)0x9F) ? '\u20AC' : super.getUnicode(byteIndex);
    }
}
