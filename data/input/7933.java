public class CharToByteCp1047 extends CharToByteSingleByte {
    private final static IBM1047 nioCoder = new IBM1047();
    public String getCharacterEncoding() {
        return "Cp1047";
    }
    public CharToByteCp1047() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
