public class CharToByteCp834 extends CharToByteDBCS_ASCII {
    public CharToByteCp834() {
       super((DoubleByte.Encoder)new IBM834().newEncoder());
       subBytes = new byte[] {(byte)0xfe, (byte)0xfe};
    }
    public int getMaxBytesPerChar() {
       return 2;
    }
    public String getCharacterEncoding() {
       return "Cp834";
    }
}
