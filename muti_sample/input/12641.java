public class CharToByteCp1141 extends CharToByteSingleByte {
    private final static IBM1141 nioCoder = new IBM1141();
    public String getCharacterEncoding() {
        return "Cp1141";
    }
    public CharToByteCp1141() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
