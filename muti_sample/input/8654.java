public class CharToByteMacTurkish extends CharToByteSingleByte {
    private final static MacTurkish nioCoder = new MacTurkish();
    public String getCharacterEncoding() {
        return "MacTurkish";
    }
    public CharToByteMacTurkish() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
