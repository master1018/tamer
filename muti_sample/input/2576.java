public class CharToByteCp1112 extends CharToByteSingleByte {
    private final static IBM1112 nioCoder = new IBM1112();
    public String getCharacterEncoding() {
        return "Cp1112";
    }
    public CharToByteCp1112() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
