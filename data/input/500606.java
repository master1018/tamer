public class PrivateKeyUsagePeriod {
    private final Date notBeforeDate;
    private final Date notAfterDate;
    private byte[] encoding;
    public PrivateKeyUsagePeriod(Date notBeforeDate, Date notAfterDate) {
        this(notBeforeDate, notAfterDate, null); 
    }
    private PrivateKeyUsagePeriod(Date notBeforeDate, 
                                  Date notAfterDate, byte[] encoding) {
        this.notBeforeDate = notBeforeDate;
        this.notAfterDate = notAfterDate;
        this.encoding = encoding;
    }
    public Date getNotBefore() {
        return notBeforeDate;
    }
    public Date getNotAfter() {
        return notAfterDate;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            new ASN1Implicit(0, ASN1GeneralizedTime.getInstance()), 
            new ASN1Implicit(1, ASN1GeneralizedTime.getInstance()) }) {
        {
            setOptional(0);
            setOptional(1);
        }
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[])in.content;
            return 
                new PrivateKeyUsagePeriod((Date) values[0], (Date) values[1],
                        in.getEncoded());
        }
        protected void getValues(Object object, Object[] values) {
            PrivateKeyUsagePeriod pkup = (PrivateKeyUsagePeriod) object;
            values[0] = pkup.notBeforeDate;
            values[1] = pkup.notAfterDate;
        }
    };
}
