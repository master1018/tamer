public class ByteToCharPCK extends ByteToCharSJIS {
    ByteToCharJIS0201 bcJIS0201 = new ByteToCharJIS0201();
    ByteToCharJIS0208_Solaris bcJIS0208 = new ByteToCharJIS0208_Solaris();
    public String getCharacterEncoding() {
        return "PCK";
    }
    protected char convSingleByte(int b) {
        if ((b & 0xFF80) == 0) {
            return (char)b;
        }
        return bcJIS0201.getUnicode(b);
    }
    protected char getUnicode(int c1, int c2) {
        char outChar;
        if ((outChar = super.getUnicode(c1, c2)) != '\uFFFD') {
           return ((outChar != '\u2014')? outChar: '\u2015');
        } else {
            int adjust = c2 < 0x9F ? 1 : 0;
            int rowOffset = c1 < 0xA0 ? 0x70 : 0xB0;
            int cellOffset = (adjust == 1) ? (c2 > 0x7F ? 0x20 : 0x1F) : 0x7E;
            int b1 = ((c1 - rowOffset) << 1) - adjust;
            int b2 = c2 - cellOffset;
            outChar = bcJIS0208.getUnicode(b1, b2);
            return outChar;
        }
    }
    String prt(int i) {
        return Integer.toString(i,16);
    }
}
