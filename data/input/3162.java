public class CharToByteTIS620 extends CharToByteSingleByte {
    private final static TIS_620 nioCoder = new TIS_620();
    public String getCharacterEncoding() {
        return "TIS620";
    }
    public CharToByteTIS620() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
