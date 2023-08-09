public class CharToByteCp838 extends CharToByteSingleByte {
    private final static IBM838 nioCoder = new IBM838();
    public String getCharacterEncoding() {
        return "Cp838";
    }
    public CharToByteCp838() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
