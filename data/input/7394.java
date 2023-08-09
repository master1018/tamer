public class CharToByteMacRomania extends CharToByteSingleByte {
    private final static MacRomania nioCoder = new MacRomania();
    public String getCharacterEncoding() {
        return "MacRomania";
    }
    public CharToByteMacRomania() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
