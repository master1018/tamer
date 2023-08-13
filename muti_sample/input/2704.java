public class CRLNumberExtension extends Extension
implements CertAttrSet<String> {
    public static final String NAME = "CRLNumber";
    public static final String NUMBER = "value";
    private static final String LABEL = "CRL Number";
    private BigInteger crlNumber = null;
    private String extensionName;
    private String extensionLabel;
    private void encodeThis() throws IOException {
        if (crlNumber == null) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream os = new DerOutputStream();
        os.putInteger(this.crlNumber);
        this.extensionValue = os.toByteArray();
    }
    public CRLNumberExtension(int crlNum) throws IOException {
        this(PKIXExtensions.CRLNumber_Id, false, BigInteger.valueOf(crlNum),
        NAME, LABEL);
    }
    public CRLNumberExtension(BigInteger crlNum) throws IOException {
        this(PKIXExtensions.CRLNumber_Id, false, crlNum, NAME, LABEL);
    }
    protected CRLNumberExtension(ObjectIdentifier extensionId,
        boolean isCritical, BigInteger crlNum, String extensionName,
        String extensionLabel) throws IOException {
        this.extensionId = extensionId;
        this.critical = isCritical;
        this.crlNumber = crlNum;
        this.extensionName = extensionName;
        this.extensionLabel = extensionLabel;
        encodeThis();
    }
    public CRLNumberExtension(Boolean critical, Object value)
    throws IOException {
        this(PKIXExtensions.CRLNumber_Id, critical, value, NAME, LABEL);
    }
    protected CRLNumberExtension(ObjectIdentifier extensionId,
        Boolean critical, Object value, String extensionName,
        String extensionLabel) throws IOException {
        this.extensionId = extensionId;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        DerValue val = new DerValue(this.extensionValue);
        this.crlNumber = val.getBigInteger();
        this.extensionName = extensionName;
        this.extensionLabel = extensionLabel;
    }
    public void set(String name, Object obj) throws IOException {
        if (name.equalsIgnoreCase(NUMBER)) {
            if (!(obj instanceof BigInteger)) {
                throw new IOException("Attribute must be of type BigInteger.");
            }
            crlNumber = (BigInteger)obj;
        } else {
          throw new IOException("Attribute name not recognized by"
                                + " CertAttrSet:" + extensionName + ".");
        }
        encodeThis();
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(NUMBER)) {
            if (crlNumber == null) return null;
            else return crlNumber;
        } else {
          throw new IOException("Attribute name not recognized by"
                                + " CertAttrSet:" + extensionName + ".");
        }
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(NUMBER)) {
            crlNumber = null;
        } else {
          throw new IOException("Attribute name not recognized by"
                                + " CertAttrSet:" + extensionName + ".");
        }
        encodeThis();
    }
    public String toString() {
        String s = super.toString() + extensionLabel + ": " +
                   ((crlNumber == null) ? "" : Debug.toHexString(crlNumber))
                   + "\n";
        return (s);
    }
    public void encode(OutputStream out) throws IOException {
       DerOutputStream  tmp = new DerOutputStream();
        encode(out, PKIXExtensions.CRLNumber_Id, true);
    }
    protected void encode(OutputStream out, ObjectIdentifier extensionId,
        boolean isCritical) throws IOException {
       DerOutputStream  tmp = new DerOutputStream();
       if (this.extensionValue == null) {
           this.extensionId = extensionId;
           this.critical = isCritical;
           encodeThis();
       }
       super.encode(tmp);
       out.write(tmp.toByteArray());
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(NUMBER);
        return (elements.elements());
    }
    public String getName() {
        return (extensionName);
    }
}
