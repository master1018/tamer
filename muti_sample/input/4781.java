public class CharToByteCp860 extends CharToByteSingleByte {
    private final static IBM860 nioCoder = new IBM860();
    public String getCharacterEncoding() {
        return "Cp860";
    }
    public CharToByteCp860() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
