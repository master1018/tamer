public class CharToByteJIS0208_Solaris extends CharToByteDoubleByte {
    public String getCharacterEncoding() {
        return "JIS0208_Solaris";
    }
    public CharToByteJIS0208_Solaris() {
        super.index1 = JIS_X_0208_Solaris_Encoder.getIndex1();
        super.index2 = JIS_X_0208_Solaris_Encoder.getIndex2();
    }
}
