public class CharToByteJIS0212 extends CharToByteDoubleByte {
    public String getCharacterEncoding() {
        return "JIS0212";
    }
    public CharToByteJIS0212() {
        super.index1 = JIS_X_0212_Encoder.getIndex1();
        super.index2 = JIS_X_0212_Encoder.getIndex2();
    }
}
