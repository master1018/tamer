public class CharToByteCp869 extends CharToByteSingleByte {
    private final static IBM869 nioCoder = new IBM869();
    public String getCharacterEncoding() {
        return "Cp869";
    }
    public CharToByteCp869() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
