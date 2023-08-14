public class ByteToCharCp1149 extends ByteToCharCp871 {
    public ByteToCharCp1149() {}
    public String getCharacterEncoding() {
        return "Cp1149";
    }
    protected char getUnicode(int byteIndex) {
        return (byteIndex == (byte)0x9F) ? '\u20AC' : super.getUnicode(byteIndex);
    }
}
