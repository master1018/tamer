public class CharToByteCp1146 extends CharToByteSingleByte {
    private final static IBM1146 nioCoder = new IBM1146();
    public String getCharacterEncoding() {
        return "Cp1146";
    }
    public CharToByteCp1146() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
