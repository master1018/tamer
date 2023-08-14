public class CertificateIssuerUniqueIdentity implements CertAttrSet<String> {
    private UniqueIdentity      id;
    public static final String IDENT = "x509.info.issuerID";
    public static final String NAME = "issuerID";
    public static final String ID = "id";
    public CertificateIssuerUniqueIdentity(UniqueIdentity id) {
        this.id = id;
    }
    public CertificateIssuerUniqueIdentity(DerInputStream in)
    throws IOException {
        id = new UniqueIdentity(in);
    }
    public CertificateIssuerUniqueIdentity(InputStream in)
    throws IOException {
        DerValue val = new DerValue(in);
        id = new UniqueIdentity(val);
    }
    public CertificateIssuerUniqueIdentity(DerValue val)
    throws IOException {
        id = new UniqueIdentity(val);
    }
    public String toString() {
        if (id == null) return "";
        return (id.toString());
    }
    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        id.encode(tmp,DerValue.createTag(DerValue.TAG_CONTEXT,false,(byte)1));
        out.write(tmp.toByteArray());
    }
    public void set(String name, Object obj) throws IOException {
        if (!(obj instanceof UniqueIdentity)) {
            throw new IOException("Attribute must be of type UniqueIdentity.");
        }
        if (name.equalsIgnoreCase(ID)) {
            id = (UniqueIdentity)obj;
        } else {
            throw new IOException("Attribute name not recognized by " +
                      "CertAttrSet: CertificateIssuerUniqueIdentity.");
        }
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(ID)) {
            return (id);
        } else {
            throw new IOException("Attribute name not recognized by " +
                      "CertAttrSet: CertificateIssuerUniqueIdentity.");
        }
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(ID)) {
            id = null;
        } else {
            throw new IOException("Attribute name not recognized by " +
                      "CertAttrSet: CertificateIssuerUniqueIdentity.");
        }
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(ID);
        return (elements.elements());
    }
    public String getName() {
        return (NAME);
    }
}
