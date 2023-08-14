public class CharToByteCp1123 extends CharToByteSingleByte {
    private final static IBM1123 nioCoder = new IBM1123();
    public String getCharacterEncoding() {
        return "Cp1123";
    }
    public CharToByteCp1123() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
