public class CharToByteCp280 extends CharToByteSingleByte {
    private final static IBM280 nioCoder = new IBM280();
    public String getCharacterEncoding() {
        return "Cp280";
    }
    public CharToByteCp280() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
