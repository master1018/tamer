public class CharToByteCp1143 extends CharToByteSingleByte {
    private final static IBM1143 nioCoder = new IBM1143();
    public String getCharacterEncoding() {
        return "Cp1143";
    }
    public CharToByteCp1143() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
