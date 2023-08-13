public class CharToByteCp1254 extends CharToByteSingleByte {
    private final static MS1254 nioCoder = new MS1254();
    public String getCharacterEncoding() {
        return "Cp1254";
    }
    public CharToByteCp1254() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
