public class CharToByteCp037 extends CharToByteSingleByte {
    private final static IBM037 nioCoder = new IBM037();
    public String getCharacterEncoding() {
        return "Cp037";
    }
    public CharToByteCp037() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
