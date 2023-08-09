public class ByteToCharJIS0212_Solaris extends ByteToCharDoubleByte {
    public String getCharacterEncoding() {
        return "JIS0212_Solaris";
    }
    protected char convSingleByte(int b) {
        return REPLACE_CHAR;
    }
    public ByteToCharJIS0212_Solaris() {
        super.index1 = JIS_X_0212_Solaris_Decoder.getIndex1();
        super.index2 = JIS_X_0212_Solaris_Decoder.getIndex2();
        start = 0x21;
        end = 0x7E;
    }
}
