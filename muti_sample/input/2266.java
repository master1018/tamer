public class CharToByteCp918 extends CharToByteSingleByte {
    private final static IBM918 nioCoder = new IBM918();
    public String getCharacterEncoding() {
        return "Cp918";
    }
    public CharToByteCp918() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
