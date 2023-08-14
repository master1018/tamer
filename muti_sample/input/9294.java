public class CharToByteCp1140 extends CharToByteSingleByte {
    private final static IBM1140 nioCoder = new IBM1140();
    public String getCharacterEncoding() {
        return "Cp1140";
    }
    public CharToByteCp1140() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
