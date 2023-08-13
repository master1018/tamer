public class ByteToCharJIS0208 extends ByteToCharDoubleByte {
    public String getCharacterEncoding() {
        return "JIS0208";
    }
    public ByteToCharJIS0208() {
        super.index1 = JIS_X_0208_Decoder.getIndex1();
        super.index2 = JIS_X_0208_Decoder.getIndex2();
        start = 0x21;
        end = 0x7E;
    }
    protected char convSingleByte(int b) {
        return REPLACE_CHAR;
    }
}
