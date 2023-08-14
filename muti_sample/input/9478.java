public class CharToByteCp862 extends CharToByteSingleByte {
    private final static IBM862 nioCoder = new IBM862();
    public String getCharacterEncoding() {
        return "Cp862";
    }
    public CharToByteCp862() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
