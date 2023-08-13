public class ByteToCharCp1148 extends ByteToCharCp500 {
    public ByteToCharCp1148() {}
    public String getCharacterEncoding() {
        return "Cp1148";
    }
    protected char getUnicode(int byteIndex) {
        return (byteIndex == (byte)0x9F) ? '\u20AC' : super.getUnicode(byteIndex);
    }
}
