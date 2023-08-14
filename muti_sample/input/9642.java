public class CharToByteCp1253 extends CharToByteSingleByte {
    private final static MS1253 nioCoder = new MS1253();
    public String getCharacterEncoding() {
        return "Cp1253";
    }
    public CharToByteCp1253() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
