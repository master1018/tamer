public class CharToByteMacRoman extends CharToByteSingleByte {
    private final static MacRoman nioCoder = new MacRoman();
    public String getCharacterEncoding() {
        return "MacRoman";
    }
    public CharToByteMacRoman() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
