public class CharToByteCp868 extends CharToByteSingleByte {
    private final static IBM868 nioCoder = new IBM868();
    public String getCharacterEncoding() {
        return "Cp868";
    }
    public CharToByteCp868() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
