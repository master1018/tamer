public class CharToByteCp871 extends CharToByteSingleByte {
    private final static IBM871 nioCoder = new IBM871();
    public String getCharacterEncoding() {
        return "Cp871";
    }
    public CharToByteCp871() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
