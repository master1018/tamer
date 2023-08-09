public class ByteToCharJIS0212 extends ByteToCharDoubleByte {
    public String getCharacterEncoding() {
        return "JIS0212";
    }
    protected char convSingleByte(int b) {
        return REPLACE_CHAR;
    }
    public ByteToCharJIS0212() {
        super.index1 = JIS_X_0212_Decoder.getIndex1();
        super.index2 = JIS_X_0212_Decoder.getIndex2();
        start = 0x21;
        end = 0x7E;
     }
}
