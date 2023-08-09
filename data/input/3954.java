public class CharToByteJIS0208 extends CharToByteDoubleByte {
    public String getCharacterEncoding() {
        return "JIS0208";
    }
    public CharToByteJIS0208() {
        super.index1 = JIS_X_0208_Encoder.getIndex1();
        super.index2 = JIS_X_0208_Encoder.getIndex2();
    }
}
