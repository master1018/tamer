public class ByteToCharUTF16 extends ByteToCharUnicode {
    public ByteToCharUTF16() {
        super(AUTO, true);
    }
    public String getCharacterEncoding() {
        return "UTF-16";
    }
}
