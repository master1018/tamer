public class CharToByteJIS0212_Solaris extends CharToByteDoubleByte {
    public String getCharacterEncoding() {
        return "JIS0212_Solaris";
    }
    public CharToByteJIS0212_Solaris() {
        super.index1 = JIS_X_0212_Solaris_Encoder.getIndex1();
        super.index2 = JIS_X_0212_Solaris_Encoder.getIndex2();
    }
}
