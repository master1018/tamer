public class CharToByteCp922 extends CharToByteSingleByte {
    private final static IBM922 nioCoder = new IBM922();
    public String getCharacterEncoding() {
        return "Cp922";
    }
    public CharToByteCp922() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
