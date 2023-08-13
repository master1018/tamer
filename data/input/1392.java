public class CharToByteCp1257 extends CharToByteSingleByte {
    private final static MS1257 nioCoder = new MS1257();
    public String getCharacterEncoding() {
        return "Cp1257";
    }
    public CharToByteCp1257() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
