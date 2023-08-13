public class ExtendedKeyUsage extends ExtensionValue {
    private List keys;
    public ExtendedKeyUsage(List keys) {
        this.keys = keys;
    }
    public ExtendedKeyUsage(byte[] encoding) {
        super(encoding);
    }
    public List getExtendedKeyUsage() throws IOException {
        if (keys == null) {
            keys = (List) ASN1.decode(getEncoded());
        }
        return keys;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(keys);
        }
        return encoding;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("Extended Key Usage: "); 
        if (keys == null) {
            try {
                keys = getExtendedKeyUsage();
            } catch (IOException e) {
                super.dumpValue(buffer);
                return;
            }
        }
        buffer.append('[');
        for (Iterator it=keys.iterator(); it.hasNext();) {
            buffer.append(" \"").append(it.next()).append('"'); 
            if (it.hasNext()) {
                buffer.append(',');
            }
        }
        buffer.append(" ]\n"); 
    }
    public static final ASN1Type ASN1 =
        new ASN1SequenceOf(new ASN1Oid() {
            public Object getDecodedObject(BerInputStream in)
                    throws IOException {
                int[] oid = (int[]) super.getDecodedObject(in);
                return ObjectIdentifier.toString(oid);
            }
        });
}
