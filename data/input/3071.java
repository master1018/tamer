public class CharToByteCp1256 extends CharToByteSingleByte {
    private final static MS1256 nioCoder = new MS1256();
    public String getCharacterEncoding() {
        return "Cp1256";
    }
    public CharToByteCp1256() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
