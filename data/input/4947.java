public class CharToByteCp1142 extends CharToByteSingleByte {
    private final static IBM1142 nioCoder = new IBM1142();
    public String getCharacterEncoding() {
        return "Cp1142";
    }
    public CharToByteCp1142() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
