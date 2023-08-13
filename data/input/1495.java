public class CharToByteCp866 extends CharToByteSingleByte {
    private final static IBM866 nioCoder = new IBM866();
    public String getCharacterEncoding() {
        return "Cp866";
    }
    public CharToByteCp866() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
