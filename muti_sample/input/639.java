public class CharToByteEUC_KR extends CharToByteDBCS_ASCII {
    private static DoubleByte.Encoder enc =
        (DoubleByte.Encoder)new EUC_KR().newEncoder();
    public String getCharacterEncoding() {
        return "EUC_KR";
    }
    public CharToByteEUC_KR() {
        super(enc);
    }
}
