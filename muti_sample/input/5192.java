public class CharToByteCp1046 extends CharToByteSingleByte {
    private final static IBM1046 nioCoder = new IBM1046();
    public String getCharacterEncoding() {
        return "Cp1046";
    }
    public CharToByteCp1046() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
