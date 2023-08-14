public class CharToByteMacSymbol extends CharToByteSingleByte {
    private final static MacSymbol nioCoder = new MacSymbol();
    public String getCharacterEncoding() {
        return "MacSymbol";
    }
    public CharToByteMacSymbol() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
