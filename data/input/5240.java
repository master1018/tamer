public class CharToByteMacCentralEurope extends CharToByteSingleByte {
    private final static MacCentralEurope nioCoder = new MacCentralEurope();
    public String getCharacterEncoding() {
        return "MacCentralEurope";
    }
    public CharToByteMacCentralEurope() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
