public class SubjectKeyIdentifierExtension extends Extension
implements CertAttrSet<String> {
    public static final String IDENT =
                         "x509.info.extensions.SubjectKeyIdentifier";
    public static final String NAME = "SubjectKeyIdentifier";
    public static final String KEY_ID = "key_id";
    private KeyIdentifier id = null;
    private void encodeThis() throws IOException {
        if (id == null) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream os = new DerOutputStream();
        id.encode(os);
        this.extensionValue = os.toByteArray();
    }
    public SubjectKeyIdentifierExtension(byte[] octetString)
    throws IOException {
        id = new KeyIdentifier(octetString);
        this.extensionId = PKIXExtensions.SubjectKey_Id;
        this.critical = false;
        encodeThis();
    }
    public SubjectKeyIdentifierExtension(Boolean critical, Object value)
    throws IOException {
        this.extensionId = PKIXExtensions.SubjectKey_Id;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        DerValue val = new DerValue(this.extensionValue);
        this.id = new KeyIdentifier(val);
    }
    public String toString() {
        return super.toString() + "SubjectKeyIdentifier [\n"
                + String.valueOf(id) + "]\n";
    }
    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (extensionValue == null) {
            extensionId = PKIXExtensions.SubjectKey_Id;
            critical = false;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }
    public void set(String name, Object obj) throws IOException {
        if (name.equalsIgnoreCase(KEY_ID)) {
            if (!(obj instanceof KeyIdentifier)) {
              throw new IOException("Attribute value should be of" +
                                    " type KeyIdentifier.");
            }
            id = (KeyIdentifier)obj;
        } else {
          throw new IOException("Attribute name not recognized by " +
                "CertAttrSet:SubjectKeyIdentifierExtension.");
        }
        encodeThis();
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(KEY_ID)) {
            return (id);
        } else {
          throw new IOException("Attribute name not recognized by " +
                "CertAttrSet:SubjectKeyIdentifierExtension.");
        }
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(KEY_ID)) {
            id = null;
        } else {
          throw new IOException("Attribute name not recognized by " +
                "CertAttrSet:SubjectKeyIdentifierExtension.");
        }
        encodeThis();
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(KEY_ID);
        return (elements.elements());
    }
    public String getName() {
        return (NAME);
    }
}
