public class CharToByteCp1149 extends CharToByteSingleByte {
    private final static IBM1149 nioCoder = new IBM1149();
    public String getCharacterEncoding() {
        return "Cp1149";
    }
    public CharToByteCp1149() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
