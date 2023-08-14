public class CharToByteMacCroatian extends CharToByteSingleByte {
    private final static MacCroatian nioCoder = new MacCroatian();
    public String getCharacterEncoding() {
        return "MacCroatian";
    }
    public CharToByteMacCroatian() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
