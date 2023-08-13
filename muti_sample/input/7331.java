public class CharToByteCp1026 extends CharToByteSingleByte {
    private final static IBM1026 nioCoder = new IBM1026();
    public String getCharacterEncoding() {
        return "Cp1026";
    }
    public CharToByteCp1026() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
