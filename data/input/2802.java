public class InhibitAnyPolicyExtension extends Extension
implements CertAttrSet<String> {
    private static final Debug debug = Debug.getInstance("certpath");
    public static final String IDENT = "x509.info.extensions.InhibitAnyPolicy";
    public static ObjectIdentifier AnyPolicy_Id;
    static {
        try {
            AnyPolicy_Id = new ObjectIdentifier("2.5.29.32.0");
        } catch (IOException ioe) {
        }
    }
    public static final String NAME = "InhibitAnyPolicy";
    public static final String SKIP_CERTS = "skip_certs";
    private int skipCerts = Integer.MAX_VALUE;
    private void encodeThis() throws IOException {
        DerOutputStream out = new DerOutputStream();
        out.putInteger(skipCerts);
        this.extensionValue = out.toByteArray();
    }
    public InhibitAnyPolicyExtension(int skipCerts) throws IOException {
        if (skipCerts < -1)
            throw new IOException("Invalid value for skipCerts");
        if (skipCerts == -1)
            this.skipCerts = Integer.MAX_VALUE;
        else
            this.skipCerts = skipCerts;
        this.extensionId = PKIXExtensions.InhibitAnyPolicy_Id;
        critical = true;
        encodeThis();
    }
    public InhibitAnyPolicyExtension(Boolean critical, Object value)
        throws IOException {
        this.extensionId = PKIXExtensions.InhibitAnyPolicy_Id;
        if (!critical.booleanValue())
            throw new IOException("Criticality cannot be false for " +
                                  "InhibitAnyPolicy");
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        DerValue val = new DerValue(this.extensionValue);
        if (val.tag != DerValue.tag_Integer)
            throw new IOException("Invalid encoding of InhibitAnyPolicy: "
                                  + "data not integer");
        if (val.data == null)
            throw new IOException("Invalid encoding of InhibitAnyPolicy: "
                                  + "null data");
        int skipCertsValue = val.getInteger();
        if (skipCertsValue < -1)
            throw new IOException("Invalid value for skipCerts");
        if (skipCertsValue == -1) {
            this.skipCerts = Integer.MAX_VALUE;
        } else {
            this.skipCerts = skipCertsValue;
        }
    }
     public String toString() {
         String s = super.toString() + "InhibitAnyPolicy: " + skipCerts + "\n";
         return s;
     }
     public void encode(OutputStream out) throws IOException {
         DerOutputStream tmp = new DerOutputStream();
         if (extensionValue == null) {
             this.extensionId = PKIXExtensions.InhibitAnyPolicy_Id;
             critical = true;
             encodeThis();
         }
         super.encode(tmp);
         out.write(tmp.toByteArray());
     }
    public void set(String name, Object obj) throws IOException {
        if (name.equalsIgnoreCase(SKIP_CERTS)) {
            if (!(obj instanceof Integer))
                throw new IOException("Attribute value should be of type Integer.");
            int skipCertsValue = ((Integer)obj).intValue();
            if (skipCertsValue < -1)
                throw new IOException("Invalid value for skipCerts");
            if (skipCertsValue == -1) {
                skipCerts = Integer.MAX_VALUE;
            } else {
                skipCerts = skipCertsValue;
            }
        } else
            throw new IOException("Attribute name not recognized by " +
                                  "CertAttrSet:InhibitAnyPolicy.");
        encodeThis();
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(SKIP_CERTS))
            return (new Integer(skipCerts));
        else
            throw new IOException("Attribute name not recognized by " +
                                  "CertAttrSet:InhibitAnyPolicy.");
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(SKIP_CERTS))
            throw new IOException("Attribute " + SKIP_CERTS +
                                  " may not be deleted.");
        else
            throw new IOException("Attribute name not recognized by " +
                                  "CertAttrSet:InhibitAnyPolicy.");
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(SKIP_CERTS);
        return (elements.elements());
    }
    public String getName() {
        return (NAME);
    }
}
