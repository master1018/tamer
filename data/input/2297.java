public class CertificateAlgorithmId implements CertAttrSet<String> {
    private AlgorithmId algId;
    public static final String IDENT = "x509.info.algorithmID";
    public static final String NAME = "algorithmID";
    public static final String ALGORITHM = "algorithm";
    public CertificateAlgorithmId(AlgorithmId algId) {
        this.algId = algId;
    }
    public CertificateAlgorithmId(DerInputStream in) throws IOException {
        DerValue val = in.getDerValue();
        algId = AlgorithmId.parse(val);
    }
    public CertificateAlgorithmId(InputStream in) throws IOException {
        DerValue val = new DerValue(in);
        algId = AlgorithmId.parse(val);
    }
    public String toString() {
        if (algId == null) return "";
        return (algId.toString() +
                ", OID = " + (algId.getOID()).toString() + "\n");
    }
    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        algId.encode(tmp);
        out.write(tmp.toByteArray());
    }
    public void set(String name, Object obj) throws IOException {
        if (!(obj instanceof AlgorithmId)) {
            throw new IOException("Attribute must be of type AlgorithmId.");
        }
        if (name.equalsIgnoreCase(ALGORITHM)) {
            algId = (AlgorithmId)obj;
        } else {
            throw new IOException("Attribute name not recognized by " +
                              "CertAttrSet:CertificateAlgorithmId.");
        }
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(ALGORITHM)) {
            return (algId);
        } else {
            throw new IOException("Attribute name not recognized by " +
                               "CertAttrSet:CertificateAlgorithmId.");
        }
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(ALGORITHM)) {
            algId = null;
        } else {
            throw new IOException("Attribute name not recognized by " +
                               "CertAttrSet:CertificateAlgorithmId.");
        }
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(ALGORITHM);
        return (elements.elements());
    }
   public String getName() {
      return (NAME);
   }
}
