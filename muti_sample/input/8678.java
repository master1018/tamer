public class CharToByteMacArabic extends CharToByteSingleByte {
    private final static MacArabic nioCoder = new MacArabic();
    public String getCharacterEncoding() {
        return "MacArabic";
    }
    public CharToByteMacArabic() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
