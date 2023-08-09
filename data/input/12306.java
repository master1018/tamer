public class CharToByteCp930 extends CharToByteDBCS_EBCDIC {
    public String getCharacterEncoding() {
        return "Cp930";
    }
    public CharToByteCp930() {
        super((DoubleByte.Encoder)new IBM930().newEncoder());
    }
}
