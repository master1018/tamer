public class ByteToCharCp1145 extends ByteToCharCp284 {
    public ByteToCharCp1145() {}
    public String getCharacterEncoding() {
        return "Cp1145";
    }
    protected char getUnicode(int byteIndex) {
        return (byteIndex == (byte)0x9F) ? '\u20AC' : super.getUnicode(byteIndex);
    }
}
