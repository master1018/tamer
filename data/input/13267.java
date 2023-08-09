public class CharToByteCp852 extends CharToByteSingleByte {
    private final static IBM852 nioCoder = new IBM852();
    public String getCharacterEncoding() {
        return "Cp852";
    }
    public CharToByteCp852() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
