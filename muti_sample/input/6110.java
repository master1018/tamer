public class CharToByteCp285 extends CharToByteSingleByte {
    private final static IBM285 nioCoder = new IBM285();
    public String getCharacterEncoding() {
        return "Cp285";
    }
    public CharToByteCp285() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
