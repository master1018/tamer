public class CharToByteMacHebrew extends CharToByteSingleByte {
    private final static MacHebrew nioCoder = new MacHebrew();
    public String getCharacterEncoding() {
        return "MacHebrew";
    }
    public CharToByteMacHebrew() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
