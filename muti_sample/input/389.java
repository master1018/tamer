public class CharToByteMacIceland extends CharToByteSingleByte {
    private final static MacIceland nioCoder = new MacIceland();
    public String getCharacterEncoding() {
        return "MacIceland";
    }
    public CharToByteMacIceland() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
