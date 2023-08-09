public class ByteToCharMacArabic extends ByteToCharSingleByte {
    private final static MacArabic nioCoder = new MacArabic();
    public String getCharacterEncoding() {
        return "MacArabic";
    }
    public ByteToCharMacArabic() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
