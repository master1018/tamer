public class CharToByteCp1145 extends CharToByteSingleByte {
    private final static IBM1145 nioCoder = new IBM1145();
    public String getCharacterEncoding() {
        return "Cp1145";
    }
    public CharToByteCp1145() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
