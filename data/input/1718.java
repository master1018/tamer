public class CharToByteCp1144 extends CharToByteSingleByte {
    private final static IBM1144 nioCoder = new IBM1144();
    public String getCharacterEncoding() {
        return "Cp1144";
    }
    public CharToByteCp1144() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
