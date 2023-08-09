public class CertificateIssuer extends ExtensionValue {
    private X500Principal issuer;
    public CertificateIssuer(GeneralName issuer) {
        super(ASN1.encode(issuer));
    }
    public CertificateIssuer(byte[] encoding) {
        super(encoding);
    }
    public X500Principal getIssuer() throws IOException {
        if (issuer == null) {
            issuer = (X500Principal) ASN1.decode(getEncoded());
        }
        return issuer;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("Certificate Issuer: "); 
        if (issuer == null) {
            try {
                issuer = getIssuer();
            } catch (IOException e) {
                buffer.append("Unparseable (incorrect!) extension value:\n"); 
                super.dumpValue(buffer);
            }
        }
        buffer.append(issuer).append('\n');
    }
    public static final ASN1Type ASN1 = new ASN1Sequence(new ASN1Type[] {
        GeneralName.ASN1
    }) {
        public Object getDecodedObject(BerInputStream in) {
            return ((Name) ((GeneralName) ((Object[]) in.content)[0])
                    .getName()).getX500Principal();
        }
        protected void getValues(Object object, Object[] values) {
            values[0] = object;
        }
    };
}
