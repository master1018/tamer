public class SubjectKeyIdentifier extends ExtensionValue {
    private final byte[] keyIdentifier;
    public SubjectKeyIdentifier(byte[] keyIdentifier) {
        this.keyIdentifier = keyIdentifier;
    }
    public static SubjectKeyIdentifier decode(byte[] encoding)
            throws IOException {
        SubjectKeyIdentifier res = new SubjectKeyIdentifier((byte[])
                ASN1OctetString.getInstance().decode(encoding));
        res.encoding = encoding;
        return res;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1OctetString.getInstance().encode(keyIdentifier);
        }
        return encoding;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("SubjectKeyIdentifier: [\n"); 
        buffer.append(Array.toString(keyIdentifier, prefix));
        buffer.append(prefix).append("]\n"); 
    }
}
