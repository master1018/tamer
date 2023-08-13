public class CharToByteCp1124 extends CharToByteSingleByte {
    private final static IBM1124 nioCoder = new IBM1124();
    public String getCharacterEncoding() {
        return "Cp1124";
    }
    public CharToByteCp1124() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
