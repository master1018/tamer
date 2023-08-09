public class CharToByteCp437 extends CharToByteSingleByte {
    private final static IBM437 nioCoder = new IBM437();
    public String getCharacterEncoding() {
        return "Cp437";
    }
    public CharToByteCp437() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
