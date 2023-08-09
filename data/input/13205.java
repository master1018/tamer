public class CharToByteCp297 extends CharToByteSingleByte {
    private final static IBM297 nioCoder = new IBM297();
    public String getCharacterEncoding() {
        return "Cp297";
    }
    public CharToByteCp297() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
