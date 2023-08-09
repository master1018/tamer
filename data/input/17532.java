public class CharToByteCp500 extends CharToByteSingleByte {
    private final static IBM500 nioCoder = new IBM500();
    public String getCharacterEncoding() {
        return "Cp500";
    }
    public CharToByteCp500() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
