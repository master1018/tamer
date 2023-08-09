public class CharToByteCp921 extends CharToByteSingleByte {
    private final static IBM921 nioCoder = new IBM921();
    public String getCharacterEncoding() {
        return "Cp921";
    }
    public CharToByteCp921() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
