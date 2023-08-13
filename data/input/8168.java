public class PrivateKeyUsageExtension extends Extension
implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.PrivateKeyUsage";
    public static final String NAME = "PrivateKeyUsage";
    public static final String NOT_BEFORE = "not_before";
    public static final String NOT_AFTER = "not_after";
    private static final byte TAG_BEFORE = 0;
    private static final byte TAG_AFTER = 1;
    private Date        notBefore = null;
    private Date        notAfter = null;
    private void encodeThis() throws IOException {
        if (notBefore == null && notAfter == null) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream seq = new DerOutputStream();
        DerOutputStream tagged = new DerOutputStream();
        if (notBefore != null) {
            DerOutputStream tmp = new DerOutputStream();
            tmp.putGeneralizedTime(notBefore);
            tagged.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT,
                                 false, TAG_BEFORE), tmp);
        }
        if (notAfter != null) {
            DerOutputStream tmp = new DerOutputStream();
            tmp.putGeneralizedTime(notAfter);
            tagged.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT,
                                 false, TAG_AFTER), tmp);
        }
        seq.write(DerValue.tag_Sequence, tagged);
        this.extensionValue = seq.toByteArray();
    }
    public PrivateKeyUsageExtension(Date notBefore, Date notAfter)
    throws IOException {
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.extensionId = PKIXExtensions.PrivateKeyUsage_Id;
        this.critical = false;
        encodeThis();
    }
    public PrivateKeyUsageExtension(Boolean critical, Object value)
    throws CertificateException, IOException {
        this.extensionId = PKIXExtensions.PrivateKeyUsage_Id;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        DerInputStream str = new DerInputStream(this.extensionValue);
        DerValue[] seq = str.getSequence(2);
        for (int i = 0; i < seq.length; i++) {
            DerValue opt = seq[i];
            if (opt.isContextSpecific(TAG_BEFORE) &&
                !opt.isConstructed()) {
                if (notBefore != null) {
                    throw new CertificateParsingException(
                        "Duplicate notBefore in PrivateKeyUsage.");
                }
                opt.resetTag(DerValue.tag_GeneralizedTime);
                str = new DerInputStream(opt.toByteArray());
                notBefore = str.getGeneralizedTime();
            } else if (opt.isContextSpecific(TAG_AFTER) &&
                       !opt.isConstructed()) {
                if (notAfter != null) {
                    throw new CertificateParsingException(
                        "Duplicate notAfter in PrivateKeyUsage.");
                }
                opt.resetTag(DerValue.tag_GeneralizedTime);
                str = new DerInputStream(opt.toByteArray());
                notAfter = str.getGeneralizedTime();
            } else
                throw new IOException("Invalid encoding of " +
                                      "PrivateKeyUsageExtension");
        }
    }
    public String toString() {
        return(super.toString() +
                "PrivateKeyUsage: [\n" +
                ((notBefore == null) ? "" : "From: " + notBefore.toString() + ", ")
                + ((notAfter == null) ? "" : "To: " + notAfter.toString())
                + "]\n");
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
    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (extensionValue == null) {
            extensionId = PKIXExtensions.PrivateKeyUsage_Id;
            critical = false;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }
    public void set(String name, Object obj)
    throws CertificateException, IOException {
        if (!(obj instanceof Date)) {
            throw new CertificateException("Attribute must be of type Date.");
        }
        if (name.equalsIgnoreCase(NOT_BEFORE)) {
            notBefore = (Date)obj;
        } else if (name.equalsIgnoreCase(NOT_AFTER)) {
            notAfter = (Date)obj;
        } else {
          throw new CertificateException("Attribute name not recognized by"
                           + " CertAttrSet:PrivateKeyUsage.");
        }
        encodeThis();
    }
    public Object get(String name) throws CertificateException {
      if (name.equalsIgnoreCase(NOT_BEFORE)) {
          return (new Date(notBefore.getTime()));
      } else if (name.equalsIgnoreCase(NOT_AFTER)) {
          return (new Date(notAfter.getTime()));
      } else {
          throw new CertificateException("Attribute name not recognized by"
                           + " CertAttrSet:PrivateKeyUsage.");
      }
  }
    public void delete(String name) throws CertificateException, IOException {
        if (name.equalsIgnoreCase(NOT_BEFORE)) {
            notBefore = null;
        } else if (name.equalsIgnoreCase(NOT_AFTER)) {
            notAfter = null;
        } else {
          throw new CertificateException("Attribute name not recognized by"
                           + " CertAttrSet:PrivateKeyUsage.");
        }
        encodeThis();
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(NOT_BEFORE);
        elements.addElement(NOT_AFTER);
        return(elements.elements());
    }
    public String getName() {
      return(NAME);
    }
}
