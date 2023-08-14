public class CharToByteCp833 extends CharToByteSingleByte {
    private final static IBM833 nioCoder = new IBM833();
    public String getCharacterEncoding() {
        return "Cp833";
    }
    public CharToByteCp833() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
