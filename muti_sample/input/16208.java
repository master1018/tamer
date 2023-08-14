public class CharToByteCp737 extends CharToByteSingleByte {
    private final static IBM737 nioCoder = new IBM737();
    public String getCharacterEncoding() {
        return "Cp737";
    }
    public CharToByteCp737() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
