public class CharToByteCp870 extends CharToByteSingleByte {
    private final static IBM870 nioCoder = new IBM870();
    public String getCharacterEncoding() {
        return "Cp870";
    }
    public CharToByteCp870() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
