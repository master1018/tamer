public class InfoAccessSyntax extends ExtensionValue {
    private final List accessDescriptions;
    public InfoAccessSyntax(List accessDescriptions) throws IOException {
        this(accessDescriptions, null);
    }
    private InfoAccessSyntax(List accessDescriptions, byte[] encoding)
            throws IOException {
        if (accessDescriptions == null || accessDescriptions.isEmpty()) {
            throw new IOException(Messages.getString("security.1A3")); 
        }
        this.accessDescriptions = accessDescriptions;
        this.encoding = encoding;
    }
    public List getAccessDescriptions() {
        return new ArrayList(accessDescriptions);
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public static InfoAccessSyntax decode(byte[] encoding) throws IOException {
        return ((InfoAccessSyntax) ASN1.decode(encoding));
    }
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("\n---- InfoAccessSyntax:"); 
        if (accessDescriptions != null) {
            for (Iterator it = accessDescriptions.iterator(); it.hasNext();) {
                res.append('\n');
                res.append(it.next());
            }
        }
        res.append("\n---- InfoAccessSyntax END\n"); 
        return res.toString();
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("AccessDescriptions:\n"); 
        if (accessDescriptions == null || accessDescriptions.isEmpty()) {
            buffer.append("NULL\n"); 
        } else {
            Iterator itr = accessDescriptions.iterator();
            while (itr.hasNext()) {
                buffer.append(itr.next().toString());
            }
        }
    }
    public static final ASN1Type ASN1 = new ASN1SequenceOf(AccessDescription.ASN1) {
        public Object getDecodedObject(BerInputStream in) throws IOException {
            return new InfoAccessSyntax((List)in.content, in.getEncoded());
        }
        public Collection getValues(Object object) {
            InfoAccessSyntax aias = (InfoAccessSyntax) object;
            return aias.accessDescriptions;
        }
    };
}
