public class ByteToCharCp1143 extends ByteToCharCp278 {
    public ByteToCharCp1143() {}
    public String getCharacterEncoding() {
        return "Cp1143";
    }
    protected char getUnicode(int byteIndex) {
        return (byteIndex == 0x5A) ? '\u20AC' : super.getUnicode(byteIndex);
    }
}
