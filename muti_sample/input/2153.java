public class CharToByteMS874 extends CharToByteSingleByte {
    private final static MS874 nioCoder = new MS874();
    public String getCharacterEncoding() {
        return "MS874";
    }
    public CharToByteMS874() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
