public class CharToByteCp863 extends CharToByteSingleByte {
    private final static IBM863 nioCoder = new IBM863();
    public String getCharacterEncoding() {
        return "Cp863";
    }
    public CharToByteCp863() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
