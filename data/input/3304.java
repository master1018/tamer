public class ByteToCharCp1140 extends ByteToCharCp037 {
    public ByteToCharCp1140() {}
    public String getCharacterEncoding() {
        return "Cp1140";
    }
    protected char getUnicode(int byteIndex) {
        return (byteIndex == (byte)0x9F) ? '\u20AC' : super.getUnicode(byteIndex);
    }
}
