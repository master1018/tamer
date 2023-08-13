public class CharToByteCp861 extends CharToByteSingleByte {
    private final static IBM861 nioCoder = new IBM861();
    public String getCharacterEncoding() {
        return "Cp861";
    }
    public CharToByteCp861() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
