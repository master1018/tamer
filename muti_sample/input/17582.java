public class ByteToCharCp1142 extends ByteToCharCp277 {
    public ByteToCharCp1142() {}
    public String getCharacterEncoding() {
        return "Cp1142";
    }
    protected char getUnicode(int byteIndex) {
        return (byteIndex == 0x5A) ? '\u20AC' : super.getUnicode(byteIndex);
    }
}
