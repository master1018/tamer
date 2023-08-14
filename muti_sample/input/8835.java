public class CharToByteCp858 extends CharToByteSingleByte {
    public String getCharacterEncoding() {
        return "Cp858";
    }
    private final static IBM858 nioCoder = new IBM858();
    public CharToByteCp858() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
