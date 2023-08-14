public class IssuingDistributionPointExtension extends Extension
        implements CertAttrSet<String> {
    public static final String IDENT =
                                "x509.info.extensions.IssuingDistributionPoint";
    public static final String NAME = "IssuingDistributionPoint";
    public static final String POINT = "point";
    public static final String REASONS = "reasons";
    public static final String ONLY_USER_CERTS = "only_user_certs";
    public static final String ONLY_CA_CERTS = "only_ca_certs";
    public static final String ONLY_ATTRIBUTE_CERTS = "only_attribute_certs";
    public static final String INDIRECT_CRL = "indirect_crl";
    private DistributionPointName distributionPoint = null;
    private ReasonFlags revocationReasons = null;
    private boolean hasOnlyUserCerts = false;
    private boolean hasOnlyCACerts = false;
    private boolean hasOnlyAttributeCerts = false;
    private boolean isIndirectCRL = false;
    private static final byte TAG_DISTRIBUTION_POINT = 0;
    private static final byte TAG_ONLY_USER_CERTS = 1;
    private static final byte TAG_ONLY_CA_CERTS = 2;
    private static final byte TAG_ONLY_SOME_REASONS = 3;
    private static final byte TAG_INDIRECT_CRL = 4;
    private static final byte TAG_ONLY_ATTRIBUTE_CERTS = 5;
    public IssuingDistributionPointExtension(
        DistributionPointName distributionPoint, ReasonFlags revocationReasons,
        boolean hasOnlyUserCerts, boolean hasOnlyCACerts,
        boolean hasOnlyAttributeCerts, boolean isIndirectCRL)
            throws IOException {
        if ((hasOnlyUserCerts && (hasOnlyCACerts || hasOnlyAttributeCerts)) ||
            (hasOnlyCACerts && (hasOnlyUserCerts || hasOnlyAttributeCerts)) ||
            (hasOnlyAttributeCerts && (hasOnlyUserCerts || hasOnlyCACerts))) {
            throw new IllegalArgumentException(
                "Only one of hasOnlyUserCerts, hasOnlyCACerts, " +
                "hasOnlyAttributeCerts may be set to true");
        }
        this.extensionId = PKIXExtensions.IssuingDistributionPoint_Id;
        this.critical = true;
        this.distributionPoint = distributionPoint;
        this.revocationReasons = revocationReasons;
        this.hasOnlyUserCerts = hasOnlyUserCerts;
        this.hasOnlyCACerts = hasOnlyCACerts;
        this.hasOnlyAttributeCerts = hasOnlyAttributeCerts;
        this.isIndirectCRL = isIndirectCRL;
        encodeThis();
    }
    public IssuingDistributionPointExtension(Boolean critical, Object value)
            throws IOException {
        this.extensionId = PKIXExtensions.IssuingDistributionPoint_Id;
        this.critical = critical.booleanValue();
        if (!(value instanceof byte[])) {
            throw new IOException("Illegal argument type");
        }
        extensionValue = (byte[])value;
        DerValue val = new DerValue(extensionValue);
        if (val.tag != DerValue.tag_Sequence) {
            throw new IOException("Invalid encoding for " +
                                  "IssuingDistributionPointExtension.");
        }
        if ((val.data == null) || (val.data.available() == 0)) {
            return;
        }
        DerInputStream in = val.data;
        while (in != null && in.available() != 0) {
            DerValue opt = in.getDerValue();
            if (opt.isContextSpecific(TAG_DISTRIBUTION_POINT) &&
                opt.isConstructed()) {
                distributionPoint =
                    new DistributionPointName(opt.data.getDerValue());
            } else if (opt.isContextSpecific(TAG_ONLY_USER_CERTS) &&
                       !opt.isConstructed()) {
                opt.resetTag(DerValue.tag_Boolean);
                hasOnlyUserCerts = opt.getBoolean();
            } else if (opt.isContextSpecific(TAG_ONLY_CA_CERTS) &&
                  !opt.isConstructed()) {
                opt.resetTag(DerValue.tag_Boolean);
                hasOnlyCACerts = opt.getBoolean();
            } else if (opt.isContextSpecific(TAG_ONLY_SOME_REASONS) &&
                       !opt.isConstructed()) {
                revocationReasons = new ReasonFlags(opt); 
            } else if (opt.isContextSpecific(TAG_INDIRECT_CRL) &&
                       !opt.isConstructed()) {
                opt.resetTag(DerValue.tag_Boolean);
                isIndirectCRL = opt.getBoolean();
            } else if (opt.isContextSpecific(TAG_ONLY_ATTRIBUTE_CERTS) &&
                       !opt.isConstructed()) {
                opt.resetTag(DerValue.tag_Boolean);
                hasOnlyAttributeCerts = opt.getBoolean();
            } else {
                throw new IOException
                    ("Invalid encoding of IssuingDistributionPoint");
            }
        }
    }
    public String getName() {
        return NAME;
    }
    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.IssuingDistributionPoint_Id;
            this.critical = false;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }
    public void set(String name, Object obj) throws IOException {
        if (name.equalsIgnoreCase(POINT)) {
            if (!(obj instanceof DistributionPointName)) {
                throw new IOException(
                    "Attribute value should be of type DistributionPointName.");
            }
            distributionPoint = (DistributionPointName)obj;
        } else if (name.equalsIgnoreCase(REASONS)) {
            if (!(obj instanceof ReasonFlags)) {
                throw new IOException(
                    "Attribute value should be of type ReasonFlags.");
            }
        } else if (name.equalsIgnoreCase(INDIRECT_CRL)) {
            if (!(obj instanceof Boolean)) {
                throw new IOException(
                    "Attribute value should be of type Boolean.");
            }
            isIndirectCRL = ((Boolean)obj).booleanValue();
        } else if (name.equalsIgnoreCase(ONLY_USER_CERTS)) {
            if (!(obj instanceof Boolean)) {
                throw new IOException(
                    "Attribute value should be of type Boolean.");
            }
            hasOnlyUserCerts = ((Boolean)obj).booleanValue();
        } else if (name.equalsIgnoreCase(ONLY_CA_CERTS)) {
            if (!(obj instanceof Boolean)) {
                throw new IOException(
                    "Attribute value should be of type Boolean.");
            }
            hasOnlyCACerts = ((Boolean)obj).booleanValue();
        } else if (name.equalsIgnoreCase(ONLY_ATTRIBUTE_CERTS)) {
            if (!(obj instanceof Boolean)) {
                throw new IOException(
                    "Attribute value should be of type Boolean.");
            }
            hasOnlyAttributeCerts = ((Boolean)obj).booleanValue();
        } else {
            throw new IOException("Attribute name [" + name +
                "] not recognized by " +
                "CertAttrSet:IssuingDistributionPointExtension.");
        }
        encodeThis();
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(POINT)) {
            return distributionPoint;
        } else if (name.equalsIgnoreCase(INDIRECT_CRL)) {
            return Boolean.valueOf(isIndirectCRL);
        } else if (name.equalsIgnoreCase(REASONS)) {
            return revocationReasons;
        } else if (name.equalsIgnoreCase(ONLY_USER_CERTS)) {
            return Boolean.valueOf(hasOnlyUserCerts);
        } else if (name.equalsIgnoreCase(ONLY_CA_CERTS)) {
            return Boolean.valueOf(hasOnlyCACerts);
        } else if (name.equalsIgnoreCase(ONLY_ATTRIBUTE_CERTS)) {
            return Boolean.valueOf(hasOnlyAttributeCerts);
        } else {
            throw new IOException("Attribute name [" + name +
                "] not recognized by " +
                "CertAttrSet:IssuingDistributionPointExtension.");
        }
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(POINT)) {
            distributionPoint = null;
        } else if (name.equalsIgnoreCase(INDIRECT_CRL)) {
            isIndirectCRL = false;
        } else if (name.equalsIgnoreCase(REASONS)) {
            revocationReasons = null;
        } else if (name.equalsIgnoreCase(ONLY_USER_CERTS)) {
            hasOnlyUserCerts = false;
        } else if (name.equalsIgnoreCase(ONLY_CA_CERTS)) {
            hasOnlyCACerts = false;
        } else if (name.equalsIgnoreCase(ONLY_ATTRIBUTE_CERTS)) {
            hasOnlyAttributeCerts = false;
        } else {
            throw new IOException("Attribute name [" + name +
                "] not recognized by " +
                "CertAttrSet:IssuingDistributionPointExtension.");
        }
        encodeThis();
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(POINT);
        elements.addElement(REASONS);
        elements.addElement(ONLY_USER_CERTS);
        elements.addElement(ONLY_CA_CERTS);
        elements.addElement(ONLY_ATTRIBUTE_CERTS);
        elements.addElement(INDIRECT_CRL);
        return elements.elements();
    }
    private void encodeThis() throws IOException {
        if (distributionPoint == null &&
            revocationReasons == null &&
            !hasOnlyUserCerts &&
            !hasOnlyCACerts &&
            !hasOnlyAttributeCerts &&
            !isIndirectCRL) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream tagged = new DerOutputStream();
        if (distributionPoint != null) {
            DerOutputStream tmp = new DerOutputStream();
            distributionPoint.encode(tmp);
            tagged.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, true,
                TAG_DISTRIBUTION_POINT), tmp);
        }
        if (hasOnlyUserCerts) {
            DerOutputStream tmp = new DerOutputStream();
            tmp.putBoolean(hasOnlyUserCerts);
            tagged.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, false,
                TAG_ONLY_USER_CERTS), tmp);
        }
        if (hasOnlyCACerts) {
            DerOutputStream tmp = new DerOutputStream();
            tmp.putBoolean(hasOnlyCACerts);
            tagged.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, false,
                TAG_ONLY_CA_CERTS), tmp);
        }
        if (revocationReasons != null) {
            DerOutputStream tmp = new DerOutputStream();
            revocationReasons.encode(tmp);
            tagged.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, false,
                TAG_ONLY_SOME_REASONS), tmp);
        }
        if (isIndirectCRL) {
            DerOutputStream tmp = new DerOutputStream();
            tmp.putBoolean(isIndirectCRL);
            tagged.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, false,
                TAG_INDIRECT_CRL), tmp);
        }
        if (hasOnlyAttributeCerts) {
            DerOutputStream tmp = new DerOutputStream();
            tmp.putBoolean(hasOnlyAttributeCerts);
            tagged.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, false,
                TAG_ONLY_ATTRIBUTE_CERTS), tmp);
        }
        DerOutputStream seq = new DerOutputStream();
        seq.write(DerValue.tag_Sequence, tagged);
        this.extensionValue = seq.toByteArray();
    }
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("IssuingDistributionPoint [\n  ");
        if (distributionPoint != null) {
            sb.append(distributionPoint);
        }
        if (revocationReasons != null) {
            sb.append(revocationReasons);
        }
        sb.append((hasOnlyUserCerts)
                ? ("  Only contains user certs: true")
                : ("  Only contains user certs: false")).append("\n");
        sb.append((hasOnlyCACerts)
                ? ("  Only contains CA certs: true")
                : ("  Only contains CA certs: false")).append("\n");
        sb.append((hasOnlyAttributeCerts)
                ? ("  Only contains attribute certs: true")
                : ("  Only contains attribute certs: false")).append("\n");
        sb.append((isIndirectCRL)
                ? ("  Indirect CRL: true")
                : ("  Indirect CRL: false")).append("\n");
        sb.append("]\n");
        return sb.toString();
    }
}
