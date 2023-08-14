public class CharToByteCp1025 extends CharToByteSingleByte {
    private final static IBM1025 nioCoder = new IBM1025();
    public String getCharacterEncoding() {
        return "Cp1025";
    }
    public CharToByteCp1025() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
