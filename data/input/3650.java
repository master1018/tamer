public class CharToByteISO8859_7 extends CharToByteSingleByte {
    public String getCharacterEncoding() {
        return "ISO8859_7";
    }
    private final static ISO_8859_7 nioCoder = new ISO_8859_7();
    public CharToByteISO8859_7() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
