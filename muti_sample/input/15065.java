public class CharToByteCp1255 extends CharToByteSingleByte {
    private final static MS1255 nioCoder = new MS1255();
    public String getCharacterEncoding() {
        return "Cp1255";
    }
    public CharToByteCp1255() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
