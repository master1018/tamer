public class CharToByteCp1147 extends CharToByteSingleByte {
    private final static IBM1147 nioCoder = new IBM1147();
    public String getCharacterEncoding() {
        return "Cp1147";
    }
    public CharToByteCp1147() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
