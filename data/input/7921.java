public class CharToByteEUC_CN extends CharToByteDBCS_ASCII {
    private static DoubleByte.Encoder enc =
        (DoubleByte.Encoder)new EUC_CN().newEncoder();
    public String getCharacterEncoding() {
        return "EUC_CN";
    }
    public CharToByteEUC_CN() {
        super(enc);
    }
}
