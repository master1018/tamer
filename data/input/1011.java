public class CharToByteCp850 extends CharToByteSingleByte {
    private final static IBM850 nioCoder = new IBM850();
    public String getCharacterEncoding() {
        return "Cp850";
    }
    public CharToByteCp850() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
