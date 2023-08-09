public class CharToByteCp277 extends CharToByteSingleByte {
    private final static IBM277 nioCoder = new IBM277();
    public String getCharacterEncoding() {
        return "Cp277";
    }
    public CharToByteCp277() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
