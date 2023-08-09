public class CharToByteCp1252 extends CharToByteSingleByte {
    private final static MS1252 nioCoder = new MS1252();
    public String getCharacterEncoding() {
        return "Cp1252";
    }
    public CharToByteCp1252() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
