public class CertificateValidity implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.validity";
    public static final String NAME = "validity";
    public static final String NOT_BEFORE = "notBefore";
    public static final String NOT_AFTER = "notAfter";
    private static final long YR_2050 = 2524636800000L;
    private Date        notBefore;
    private Date        notAfter;
    private Date getNotBefore() {
        return (new Date(notBefore.getTime()));
    }
    private Date getNotAfter() {
       return (new Date(notAfter.getTime()));
    }
    private void construct(DerValue derVal) throws IOException {
        if (derVal.tag != DerValue.tag_Sequence) {
            throw new IOException("Invalid encoded CertificateValidity, " +
                                  "starting sequence tag missing.");
        }
        if (derVal.data.available() == 0)
            throw new IOException("No data encoded for CertificateValidity");
        DerInputStream derIn = new DerInputStream(derVal.toByteArray());
        DerValue[] seq = derIn.getSequence(2);
        if (seq.length != 2)
            throw new IOException("Invalid encoding for CertificateValidity");
        if (seq[0].tag == DerValue.tag_UtcTime) {
            notBefore = derVal.data.getUTCTime();
        } else if (seq[0].tag == DerValue.tag_GeneralizedTime) {
            notBefore = derVal.data.getGeneralizedTime();
        } else {
            throw new IOException("Invalid encoding for CertificateValidity");
        }
        if (seq[1].tag == DerValue.tag_UtcTime) {
            notAfter = derVal.data.getUTCTime();
        } else if (seq[1].tag == DerValue.tag_GeneralizedTime) {
            notAfter = derVal.data.getGeneralizedTime();
        } else {
            throw new IOException("Invalid encoding for CertificateValidity");
        }
    }
    public CertificateValidity() { }
    public CertificateValidity(Date notBefore, Date notAfter) {
        this.notBefore = notBefore;
        this.notAfter = notAfter;
    }
    public CertificateValidity(DerInputStream in) throws IOException {
        DerValue derVal = in.getDerValue();
        construct(derVal);
    }
    public String toString() {
        if (notBefore == null || notAfter == null)
            return "";
        return ("Validity: [From: " + notBefore.toString() +
             ",\n               To: " + notAfter.toString() + "]");
    }
    public void encode(OutputStream out) throws IOException {
        if (notBefore == null || notAfter == null) {
            throw new IOException("CertAttrSet:CertificateValidity:" +
                                  " null values to encode.\n");
        }
        DerOutputStream pair = new DerOutputStream();
        if (notBefore.getTime() < YR_2050) {
            pair.putUTCTime(notBefore);
        } else
            pair.putGeneralizedTime(notBefore);
        if (notAfter.getTime() < YR_2050) {
            pair.putUTCTime(notAfter);
        } else {
            pair.putGeneralizedTime(notAfter);
        }
        DerOutputStream seq = new DerOutputStream();
        seq.write(DerValue.tag_Sequence, pair);
        out.write(seq.toByteArray());
    }
    public void set(String name, Object obj) throws IOException {
        if (!(obj instanceof Date)) {
            throw new IOException("Attribute must be of type Date.");
        }
        if (name.equalsIgnoreCase(NOT_BEFORE)) {
            notBefore = (Date)obj;
        } else if (name.equalsIgnoreCase(NOT_AFTER)) {
            notAfter = (Date)obj;
        } else {
            throw new IOException("Attribute name not recognized by " +
                            "CertAttrSet: CertificateValidity.");
        }
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(NOT_BEFORE)) {
            return (getNotBefore());
        } else if (name.equalsIgnoreCase(NOT_AFTER)) {
            return (getNotAfter());
        } else {
            throw new IOException("Attribute name not recognized by " +
                            "CertAttrSet: CertificateValidity.");
        }
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(NOT_BEFORE)) {
            notBefore = null;
        } else if (name.equalsIgnoreCase(NOT_AFTER)) {
            notAfter = null;
        } else {
            throw new IOException("Attribute name not recognized by " +
                            "CertAttrSet: CertificateValidity.");
        }
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(NOT_BEFORE);
        elements.addElement(NOT_AFTER);
        return (elements.elements());
    }
    public String getName() {
        return (NAME);
    }
    public void valid()
    throws CertificateNotYetValidException, CertificateExpiredException {
        Date now = new Date();
        valid(now);
    }
    public void valid(Date now)
    throws CertificateNotYetValidException, CertificateExpiredException {
        if (notBefore.after(now)) {
            throw new CertificateNotYetValidException("NotBefore: " +
                                                      notBefore.toString());
        }
        if (notAfter.before(now)) {
            throw new CertificateExpiredException("NotAfter: " +
                                                  notAfter.toString());
        }
    }
}
