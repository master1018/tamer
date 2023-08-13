public class CharToByteCp856 extends CharToByteSingleByte {
    private final static IBM856 nioCoder = new IBM856();
    public String getCharacterEncoding() {
        return "Cp856";
    }
    public CharToByteCp856() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
