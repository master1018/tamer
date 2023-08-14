public class CharToByteCp420 extends CharToByteSingleByte {
    private final static IBM420 nioCoder = new IBM420();
    public String getCharacterEncoding() {
        return "Cp420";
    }
    public CharToByteCp420() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
