public class CharToByteCp875 extends CharToByteSingleByte {
    private final static IBM875 nioCoder = new IBM875();
    public String getCharacterEncoding() {
        return "Cp875";
    }
    public CharToByteCp875() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
