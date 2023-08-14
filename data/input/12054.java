public class ByteToCharCp1144 extends ByteToCharCp280 {
    public ByteToCharCp1144() {}
    public String getCharacterEncoding() {
        return "Cp1144";
    }
    protected char getUnicode(int byteIndex) {
        return (byteIndex == (byte)0x9F) ? '\u20AC' : super.getUnicode(byteIndex);
    }
}
