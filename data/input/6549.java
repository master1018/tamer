public class ByteToCharCp1147 extends ByteToCharCp297 {
    public ByteToCharCp1147() {}
    public String getCharacterEncoding() {
        return "Cp1147";
    }
    protected char getUnicode(int byteIndex) {
        return (byteIndex == (byte)0x9F) ? '\u20AC' : super.getUnicode(byteIndex);
    }
}
