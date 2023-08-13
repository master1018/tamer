public class CharToByteKOI8_R extends CharToByteSingleByte {
    private final static KOI8_R nioCoder = new KOI8_R();
    public String getCharacterEncoding() {
        return "KOI8_R";
    }
    public CharToByteKOI8_R() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
