public class CharToByteISO8859_15 extends CharToByteSingleByte {
    private final static ISO_8859_15 nioCoder = new ISO_8859_15();
    public String getCharacterEncoding() {
        return "ISO8859_15";
    }
    public CharToByteISO8859_15() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
