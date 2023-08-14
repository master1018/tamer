public class CharToByteCp278 extends CharToByteSingleByte {
    private final static IBM278 nioCoder = new IBM278();
    public String getCharacterEncoding() {
        return "Cp278";
    }
    public CharToByteCp278() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
