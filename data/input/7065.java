public class CertificateSubjectUniqueIdentity implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.subjectID";
    public static final String NAME = "subjectID";
    public static final String ID = "id";
    private UniqueIdentity      id;
    public CertificateSubjectUniqueIdentity(UniqueIdentity id) {
        this.id = id;
    }
    public CertificateSubjectUniqueIdentity(DerInputStream in)
    throws IOException {
        id = new UniqueIdentity(in);
    }
    public CertificateSubjectUniqueIdentity(InputStream in)
    throws IOException {
        DerValue val = new DerValue(in);
        id = new UniqueIdentity(val);
    }
    public CertificateSubjectUniqueIdentity(DerValue val)
    throws IOException {
        id = new UniqueIdentity(val);
    }
    public String toString() {
        if (id == null) return "";
        return(id.toString());
    }
    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        id.encode(tmp,DerValue.createTag(DerValue.TAG_CONTEXT,false,(byte)2));
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
                      "CertAttrSet: CertificateSubjectUniqueIdentity.");
        }
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(ID)) {
            return(id);
        } else {
            throw new IOException("Attribute name not recognized by " +
                      "CertAttrSet: CertificateSubjectUniqueIdentity.");
        }
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(ID)) {
            id = null;
        } else {
            throw new IOException("Attribute name not recognized by " +
                      "CertAttrSet: CertificateSubjectUniqueIdentity.");
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
