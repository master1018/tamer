public class Validity {
    private final Date notBefore;
    private final Date notAfter;
    private byte[] encoding;
    public Validity(Date notBefore, Date notAfter) {
        this.notBefore = notBefore;
        this.notAfter = notAfter;
    }
    public Date getNotBefore() {
        return notBefore;
    }
    public Date getNotAfter() {
        return notAfter;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public static final ASN1Sequence ASN1 
        = new ASN1Sequence(new ASN1Type[] {Time.ASN1, Time.ASN1 }) {
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new Validity((Date) values[0], (Date) values[1]);
        }
        protected void getValues(Object object, Object[] values) {
            Validity validity = (Validity) object;
            values[0] = validity.notBefore;
            values[1] = validity.notAfter;
        }
    };
}
