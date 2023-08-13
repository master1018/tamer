public class ByteToCharCp858 extends ByteToCharCp850 {
    public ByteToCharCp858() {}
    public String getCharacterEncoding() {
        return "Cp858";
    }
    protected char getUnicode(int byteIndex) {
        return (byteIndex == (byte)0xD5) ? '\u20AC' : super.getUnicode(byteIndex);
    }
}
