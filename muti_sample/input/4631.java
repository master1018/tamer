public class CharToByteCp1251 extends CharToByteSingleByte {
    private final static MS1251 nioCoder = new MS1251();
    public String getCharacterEncoding() {
        return "Cp1251";
    }
    public CharToByteCp1251() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
