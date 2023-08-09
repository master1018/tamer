public class CharToByteCp874 extends CharToByteSingleByte {
    private final static IBM874 nioCoder = new IBM874();
    public String getCharacterEncoding() {
        return "Cp874";
    }
    public CharToByteCp874() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
