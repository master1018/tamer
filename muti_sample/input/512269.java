public class AuthorityKeyIdentifier extends ExtensionValue {
    private final byte[] keyIdentifier;
    private final GeneralNames authorityCertIssuer;
    private final BigInteger authorityCertSerialNumber;
    public AuthorityKeyIdentifier(byte[] keyIdentifier, 
            GeneralNames authorityCertIssuer, 
            BigInteger authorityCertSerialNumber) {
        this.keyIdentifier = keyIdentifier;
        this.authorityCertIssuer = authorityCertIssuer;
        this.authorityCertSerialNumber = authorityCertSerialNumber;
    }
    public static AuthorityKeyIdentifier decode(byte[] encoding) 
            throws IOException {
        AuthorityKeyIdentifier aki =
            (AuthorityKeyIdentifier) ASN1.decode(encoding);
        aki.encoding = encoding;
        return aki;
    } 
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("AuthorityKeyIdentifier [\n"); 
        if (keyIdentifier != null) {
            buffer.append(prefix).append("  keyIdentifier:\n"); 
            buffer.append(Array.toString(keyIdentifier, prefix + "    ")); 
        }
        if (authorityCertIssuer != null) {
            buffer.append(prefix).append("  authorityCertIssuer: [\n"); 
            authorityCertIssuer.dumpValue(buffer, prefix + "    "); 
            buffer.append(prefix).append("  ]\n"); 
        }
        if (authorityCertSerialNumber != null) {
            buffer.append(prefix).append("  authorityCertSerialNumber: ") 
                .append(authorityCertSerialNumber).append('\n');
        }
        buffer.append(prefix).append("]\n"); 
    }
    public static final ASN1Type ASN1 = new ASN1Sequence(
            new ASN1Type[] {
                new ASN1Implicit(0, ASN1OctetString.getInstance()),
                new ASN1Implicit(1, GeneralNames.ASN1),
                new ASN1Implicit(2, ASN1Integer.getInstance()),
            }) {
        {
            setOptional(0);
            setOptional(1);
            setOptional(2);
        }
        protected Object getDecodedObject(BerInputStream in) throws IOException {
            Object[] values = (Object[]) in.content;
            byte[] enc = (byte[]) values[2];
            BigInteger authorityCertSerialNumber = null;
            if (enc != null) {
                authorityCertSerialNumber = new BigInteger(enc);
            }
            return new AuthorityKeyIdentifier((byte[]) values[0],
                    (GeneralNames) values[1], authorityCertSerialNumber);
        }
        protected void getValues(Object object, Object[] values) {
            AuthorityKeyIdentifier akid = (AuthorityKeyIdentifier) object;
            values[0] = akid.keyIdentifier;
            values[1] = akid.authorityCertIssuer;
            if (akid.authorityCertSerialNumber != null) {
                values[2] = akid.authorityCertSerialNumber.toByteArray();
            }
        }
    };
}
