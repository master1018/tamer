public class CharToByteMacThai extends CharToByteSingleByte {
    private final static MacThai nioCoder = new MacThai();
    public String getCharacterEncoding() {
        return "MacThai";
    }
    public CharToByteMacThai() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
