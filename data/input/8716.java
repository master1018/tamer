public class CharToByteMacCyrillic extends CharToByteSingleByte {
    private final static MacCyrillic nioCoder = new MacCyrillic();
    public String getCharacterEncoding() {
        return "MacCyrillic";
    }
    public CharToByteMacCyrillic() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
