public class CharToByteMacUkraine extends CharToByteSingleByte {
    private final static MacUkraine nioCoder = new MacUkraine();
    public String getCharacterEncoding() {
        return "MacUkraine";
    }
    public CharToByteMacUkraine() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
