public class CertificateSubjectName implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.subject";
    public static final String NAME = "subject";
    public static final String DN_NAME = "dname";
    public static final String DN_PRINCIPAL = "x500principal";
    private X500Name    dnName;
    private X500Principal dnPrincipal;
    public CertificateSubjectName(X500Name name) {
        this.dnName = name;
    }
    public CertificateSubjectName(DerInputStream in) throws IOException {
        dnName = new X500Name(in);
    }
    public CertificateSubjectName(InputStream in) throws IOException {
        DerValue derVal = new DerValue(in);
        dnName = new X500Name(derVal);
    }
    public String toString() {
        if (dnName == null) return "";
        return(dnName.toString());
    }
    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        dnName.encode(tmp);
        out.write(tmp.toByteArray());
    }
    public void set(String name, Object obj) throws IOException {
        if (!(obj instanceof X500Name)) {
            throw new IOException("Attribute must be of type X500Name.");
        }
        if (name.equalsIgnoreCase(DN_NAME)) {
            this.dnName = (X500Name)obj;
            this.dnPrincipal = null;
        } else {
            throw new IOException("Attribute name not recognized by " +
                                  "CertAttrSet:CertificateSubjectName.");
        }
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(DN_NAME)) {
            return(dnName);
        } else if (name.equalsIgnoreCase(DN_PRINCIPAL)) {
            if ((dnPrincipal == null) && (dnName != null)) {
                dnPrincipal = dnName.asX500Principal();
            }
            return dnPrincipal;
        } else {
            throw new IOException("Attribute name not recognized by " +
                                  "CertAttrSet:CertificateSubjectName.");
        }
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(DN_NAME)) {
            dnName = null;
            dnPrincipal = null;
        } else {
            throw new IOException("Attribute name not recognized by " +
                                  "CertAttrSet:CertificateSubjectName.");
        }
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(DN_NAME);
        return(elements.elements());
    }
    public String getName() {
        return(NAME);
    }
}
