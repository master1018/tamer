public class CharToByteUTF16 extends CharToByteUnicode {
    public CharToByteUTF16() {
        super(BIG, true);
    }
    public String getCharacterEncoding() {
        return "UTF-16";
    }
}
