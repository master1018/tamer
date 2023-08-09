public class ByteToCharSJIS extends ByteToCharJIS0208 {
    ByteToCharJIS0201 bcJIS0201 = new ByteToCharJIS0201();
    public String getCharacterEncoding() {
        return "SJIS";
    }
    protected char convSingleByte(int b) {
        if ((b & 0xFF80) == 0) {
            return (char)b;
        }
        return bcJIS0201.getUnicode(b);
    }
    protected char getUnicode(int c1, int c2) {
        int adjust = c2 < 0x9F ? 1 : 0;
        int rowOffset = c1 < 0xA0 ? 0x70 : 0xB0;
        int cellOffset = (adjust == 1) ? (c2 > 0x7F ? 0x20 : 0x1F) : 0x7E;
        int b1 = ((c1 - rowOffset) << 1) - adjust;
        int b2 = c2 - cellOffset;
        return super.getUnicode(b1, b2);
    }
    String prt(int i) {
        return Integer.toString(i,16);
    }
}
