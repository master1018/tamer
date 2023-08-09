public class CharToByteCp857 extends CharToByteSingleByte {
    private final static IBM857 nioCoder = new IBM857();
    public String getCharacterEncoding() {
        return "Cp857";
    }
    public CharToByteCp857() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
