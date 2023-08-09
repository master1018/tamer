public class CharToByteCp1097 extends CharToByteSingleByte {
    private final static IBM1097 nioCoder = new IBM1097();
    public String getCharacterEncoding() {
        return "Cp1097";
    }
    public CharToByteCp1097() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
