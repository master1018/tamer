public class CharToByteCp1006 extends CharToByteSingleByte {
    private final static IBM1006 nioCoder = new IBM1006();
    public String getCharacterEncoding() {
        return "Cp1006";
    }
    public CharToByteCp1006() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
