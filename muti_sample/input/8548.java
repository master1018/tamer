public class CharToByteCp1250 extends CharToByteSingleByte {
    private final static MS1250 nioCoder = new MS1250();
    public String getCharacterEncoding() {
        return "Cp1250";
    }
    public CharToByteCp1250() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
