public class InhibitAnyPolicy extends ExtensionValue {
    private int skipCerts;
    public InhibitAnyPolicy(int skipCerts) {
        this.skipCerts = skipCerts;
    }
    public InhibitAnyPolicy(byte[] encoding) throws IOException {
        super(encoding);
        this.skipCerts = new BigInteger((byte[])
                ASN1Integer.getInstance().decode(encoding)).intValue();
    }
    public int getSkipCerts() {
        return skipCerts;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1Integer.getInstance()
                .encode(ASN1Integer.fromIntValue(skipCerts));
        }
        return encoding;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("Inhibit Any-Policy: ") 
            .append(skipCerts).append('\n');
    }
}
