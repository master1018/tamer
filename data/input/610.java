public class CertificateVersion implements CertAttrSet<String> {
    public static final int     V1 = 0;
    public static final int     V2 = 1;
    public static final int     V3 = 2;
    public static final String IDENT = "x509.info.version";
    public static final String NAME = "version";
    public static final String VERSION = "number";
    int version = V1;
    private int getVersion() {
        return(version);
    }
    private void construct(DerValue derVal) throws IOException {
        if (derVal.isConstructed() && derVal.isContextSpecific()) {
            derVal = derVal.data.getDerValue();
            version = derVal.getInteger();
            if (derVal.data.available() != 0) {
                throw new IOException("X.509 version, bad format");
            }
        }
    }
    public CertificateVersion() {
        version = V1;
    }
    public CertificateVersion(int version) throws IOException {
        if (version == V1 || version == V2 || version == V3)
            this.version = version;
        else {
            throw new IOException("X.509 Certificate version " +
                                   version + " not supported.\n");
        }
    }
    public CertificateVersion(DerInputStream in) throws IOException {
        version = V1;
        DerValue derVal = in.getDerValue();
        construct(derVal);
    }
    public CertificateVersion(InputStream in) throws IOException {
        version = V1;
        DerValue derVal = new DerValue(in);
        construct(derVal);
    }
    public CertificateVersion(DerValue val) throws IOException {
        version = V1;
        construct(val);
    }
    public String toString() {
        return("Version: V" + (version+1));
    }
    public void encode(OutputStream out) throws IOException {
        if (version == V1) {
            return;
        }
        DerOutputStream tmp = new DerOutputStream();
        tmp.putInteger(version);
        DerOutputStream seq = new DerOutputStream();
        seq.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0),
                  tmp);
        out.write(seq.toByteArray());
    }
    public void set(String name, Object obj) throws IOException {
        if (!(obj instanceof Integer)) {
            throw new IOException("Attribute must be of type Integer.");
        }
        if (name.equalsIgnoreCase(VERSION)) {
            version = ((Integer)obj).intValue();
        } else {
            throw new IOException("Attribute name not recognized by " +
                                  "CertAttrSet: CertificateVersion.");
        }
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(VERSION)) {
            return(new Integer(getVersion()));
        } else {
            throw new IOException("Attribute name not recognized by " +
                                  "CertAttrSet: CertificateVersion.");
        }
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(VERSION)) {
            version = V1;
        } else {
            throw new IOException("Attribute name not recognized by " +
                                  "CertAttrSet: CertificateVersion.");
        }
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(VERSION);
        return (elements.elements());
    }
    public String getName() {
        return(NAME);
    }
    public int compare(int vers) {
        return(version - vers);
    }
}
