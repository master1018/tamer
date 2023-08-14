public class CertificateIssuerExtension extends Extension
    implements CertAttrSet<String> {
    public static final String NAME = "CertificateIssuer";
    public static final String ISSUER = "issuer";
    private GeneralNames names;
    private void encodeThis() throws IOException {
        if (names == null || names.isEmpty()) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream os = new DerOutputStream();
        names.encode(os);
        this.extensionValue = os.toByteArray();
    }
    public CertificateIssuerExtension(GeneralNames issuer) throws IOException {
        this.extensionId = PKIXExtensions.CertificateIssuer_Id;
        this.critical = true;
        this.names = issuer;
        encodeThis();
    }
    public CertificateIssuerExtension(Boolean critical, Object value)
        throws IOException {
        this.extensionId = PKIXExtensions.CertificateIssuer_Id;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        DerValue val = new DerValue(this.extensionValue);
        this.names = new GeneralNames(val);
    }
    public void set(String name, Object obj) throws IOException {
        if (name.equalsIgnoreCase(ISSUER)) {
            if (!(obj instanceof GeneralNames)) {
                throw new IOException("Attribute value must be of type " +
                    "GeneralNames");
            }
            this.names = (GeneralNames)obj;
        } else {
            throw new IOException("Attribute name not recognized by " +
                "CertAttrSet:CertificateIssuer");
        }
        encodeThis();
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(ISSUER)) {
            return names;
        } else {
            throw new IOException("Attribute name not recognized by " +
                "CertAttrSet:CertificateIssuer");
        }
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(ISSUER)) {
            names = null;
        } else {
            throw new IOException("Attribute name not recognized by " +
                "CertAttrSet:CertificateIssuer");
        }
        encodeThis();
    }
    public String toString() {
        return super.toString() + "Certificate Issuer [\n" +
            String.valueOf(names) + "]\n";
    }
    public void encode(OutputStream out) throws IOException {
        DerOutputStream  tmp = new DerOutputStream();
        if (extensionValue == null) {
            extensionId = PKIXExtensions.CertificateIssuer_Id;
            critical = true;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(ISSUER);
        return elements.elements();
    }
    public String getName() {
        return NAME;
    }
}
