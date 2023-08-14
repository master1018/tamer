public class InvalidityDate extends ExtensionValue {
    private final Date date;
    public InvalidityDate(Date date) {
        this.date = date;
    }
    public InvalidityDate(byte[] encoding) throws IOException {
        super(encoding);
        date = (Date) ASN1.decode(encoding);
    }
    public Date getDate() {
        return date;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(date);
        }
        return encoding;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("Invalidity Date: [ ") 
            .append(date).append(" ]\n"); 
    }
    public static final ASN1Type ASN1 = ASN1GeneralizedTime.getInstance();
}
