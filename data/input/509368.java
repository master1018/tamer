public class KeyUsage extends ExtensionValue {
    private static final String[] USAGES = {
        "digitalSignature", 
        "nonRepudiation", 
        "keyEncipherment", 
        "dataEncipherment", 
        "keyAgreement", 
        "keyCertSign", 
        "cRLSign", 
        "encipherOnly", 
        "decipherOnly", 
    };
    private final boolean[] keyUsage;
    public KeyUsage(boolean[] keyUsage) {
        this.keyUsage = keyUsage;
    }
    public KeyUsage(byte[] encoding) throws IOException {
        super(encoding);
        this.keyUsage = (boolean[]) ASN1.decode(encoding);
    }
    public boolean[] getKeyUsage() {
        return keyUsage;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(keyUsage);
        }
        return encoding;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("KeyUsage [\n"); 
        for (int i=0; i<keyUsage.length; i++) {
            if (keyUsage[i]) {
                buffer.append(prefix).append("  ") 
                    .append(USAGES[i]).append('\n');
            }
        }
        buffer.append(prefix).append("]\n"); 
    }
    private static final ASN1Type ASN1 = new ASN1BitString.ASN1NamedBitList(9);
}
