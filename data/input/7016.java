public class CharToByteCp284 extends CharToByteSingleByte {
    private final static IBM284 nioCoder = new IBM284();
    public String getCharacterEncoding() {
        return "Cp284";
    }
    public CharToByteCp284() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
