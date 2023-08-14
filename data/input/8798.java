public class ByteToCharJIS0208_Solaris extends ByteToCharDoubleByte {
    public String getCharacterEncoding() {
        return "JIS0208_Solaris";
    }
    protected char convSingleByte(int b) {
        return REPLACE_CHAR;
    }
    public ByteToCharJIS0208_Solaris() {
        super.index1 = JIS_X_0208_Solaris_Decoder.getIndex1();
        super.index2 = JIS_X_0208_Solaris_Decoder.getIndex2();
        start = 0x21;
        end = 0x7E;
    }
}
