public class CharToByteCp775 extends CharToByteSingleByte {
    private final static IBM775 nioCoder = new IBM775();
    public String getCharacterEncoding() {
        return "Cp775";
    }
    public CharToByteCp775() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
