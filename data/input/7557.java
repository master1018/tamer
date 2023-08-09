public class CharToByteCp1122 extends CharToByteSingleByte {
    private final static IBM1122 nioCoder = new IBM1122();
    public String getCharacterEncoding() {
        return "Cp1122";
    }
    public CharToByteCp1122() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
