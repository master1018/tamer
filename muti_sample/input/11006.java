public class CertificateX509Key implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.key";
    public static final String NAME = "key";
    public static final String KEY = "value";
    private PublicKey key;
    public CertificateX509Key(PublicKey key) {
        this.key = key;
    }
    public CertificateX509Key(DerInputStream in) throws IOException {
        DerValue val = in.getDerValue();
        key = X509Key.parse(val);
    }
    public CertificateX509Key(InputStream in) throws IOException {
        DerValue val = new DerValue(in);
        key = X509Key.parse(val);
    }
    public String toString() {
        if (key == null) return "";
        return(key.toString());
    }
    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        tmp.write(key.getEncoded());
        out.write(tmp.toByteArray());
    }
    public void set(String name, Object obj) throws IOException {
        if (name.equalsIgnoreCase(KEY)) {
            this.key = (PublicKey)obj;
        } else {
            throw new IOException("Attribute name not recognized by " +
                                  "CertAttrSet: CertificateX509Key.");
        }
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(KEY)) {
            return(key);
        } else {
            throw new IOException("Attribute name not recognized by " +
                                  "CertAttrSet: CertificateX509Key.");
        }
    }
    public void delete(String name) throws IOException {
      if (name.equalsIgnoreCase(KEY)) {
        key = null;
      } else {
            throw new IOException("Attribute name not recognized by " +
                                  "CertAttrSet: CertificateX509Key.");
      }
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(KEY);
        return(elements.elements());
    }
    public String getName() {
        return(NAME);
    }
}
