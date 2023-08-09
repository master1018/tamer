public class CharToByteCp424 extends CharToByteSingleByte {
    private final static IBM424 nioCoder = new IBM424();
    public String getCharacterEncoding() {
        return "Cp424";
    }
    public CharToByteCp424() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
