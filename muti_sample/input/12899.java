public class CharToByteMacDingbat extends CharToByteSingleByte {
    private final static MacDingbat nioCoder = new MacDingbat();
    public String getCharacterEncoding() {
        return "MacDingbat";
    }
    public CharToByteMacDingbat() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
