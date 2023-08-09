public class CharToByteCp865 extends CharToByteSingleByte {
    private final static IBM865 nioCoder = new IBM865();
    public String getCharacterEncoding() {
        return "Cp865";
    }
    public CharToByteCp865() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
