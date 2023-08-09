public class Extension {
    public static final boolean CRITICAL = true;
    public static final boolean NON_CRITICAL = false;
    static final int[] SUBJ_DIRECTORY_ATTRS = {2, 5, 29, 9};
    static final int[] SUBJ_KEY_ID = {2, 5, 29, 14};
    static final int[] KEY_USAGE = {2, 5, 29, 15};
    static final int[] PRIVATE_KEY_USAGE_PERIOD = {2, 5, 29, 16};
    static final int[] SUBJECT_ALT_NAME = {2, 5, 29, 17};
    static final int[] ISSUER_ALTERNATIVE_NAME = {2, 5, 29, 18};
    static final int[] BASIC_CONSTRAINTS = {2, 5, 29, 19};
    static final int[] NAME_CONSTRAINTS = {2, 5, 29, 30};
    static final int[] CRL_DISTR_POINTS = {2, 5, 29, 31};
    static final int[] CERTIFICATE_POLICIES = {2, 5, 29, 32};
    static final int[] POLICY_MAPPINGS = {2, 5, 29, 33};
    static final int[] AUTH_KEY_ID = {2, 5, 29, 35};
    static final int[] POLICY_CONSTRAINTS = {2, 5, 29, 36};
    static final int[] EXTENDED_KEY_USAGE = {2, 5, 29, 37};
    static final int[] FRESHEST_CRL = {2, 5, 29, 46};
    static final int[] INHIBIT_ANY_POLICY = {2, 5, 29, 54};
    static final int[] AUTHORITY_INFO_ACCESS =
                                            {1, 3, 6, 1, 5, 5, 7, 1, 1};
    static final int[] SUBJECT_INFO_ACCESS =
                                            {1, 3, 6, 1, 5, 5, 7, 1, 11};
    static final int[] ISSUING_DISTR_POINT = {2, 5, 29, 28};
    static final int[] CRL_NUMBER = {2, 5, 29, 20};
    static final int[] CERTIFICATE_ISSUER = {2, 5, 29, 29};
    static final int[] INVALIDITY_DATE = {2, 5, 29, 24};
    static final int[] REASON_CODE = {2, 5, 29, 21};
    static final int[] ISSUING_DISTR_POINTS = {2, 5, 29, 28};
    private final int[] extnID;
    private String extnID_str;
    private final boolean critical;
    private final byte[] extnValue;
    private byte[] encoding;
    private byte[] rawExtnValue;
    protected ExtensionValue extnValueObject;
    private boolean valueDecoded = false;
    public Extension(String extnID, boolean critical, 
            ExtensionValue extnValueObject) {
        this.extnID_str = extnID;
        this.extnID = ObjectIdentifier.toIntArray(extnID);
        this.critical = critical;
        this.extnValueObject = extnValueObject;
        this.valueDecoded = true;
        this.extnValue = extnValueObject.getEncoded();
    }
    public Extension(String extnID, boolean critical, byte[] extnValue) {
        this.extnID_str = extnID;
        this.extnID = ObjectIdentifier.toIntArray(extnID);
        this.critical = critical;
        this.extnValue = extnValue;
    }
    public Extension(int[] extnID, boolean critical, byte[] extnValue) {
        this.extnID = extnID;
        this.critical = critical;
        this.extnValue = extnValue;
    }
    public Extension(String extnID, byte[] extnValue) {
        this(extnID, NON_CRITICAL, extnValue);
    }
    public Extension(int[] extnID, byte[] extnValue) {
        this(extnID, NON_CRITICAL, extnValue);
    }
    private Extension(int[] extnID, boolean critical, byte[] extnValue,
            byte[] rawExtnValue, byte[] encoding, 
            ExtensionValue decodedExtValue) {
        this(extnID, critical, extnValue);
        this.rawExtnValue = rawExtnValue;
        this.encoding = encoding;
        this.extnValueObject = decodedExtValue;
        this.valueDecoded = (decodedExtValue != null);
    }
    public String getExtnID() {
        if (extnID_str == null) {
            extnID_str = ObjectIdentifier.toString(extnID);
        }
        return extnID_str;
    }
    public boolean getCritical() {
        return critical;
    }
    public byte[] getExtnValue() {
        return extnValue;
    }
    public byte[] getRawExtnValue() {
        if (rawExtnValue == null) {
            rawExtnValue = ASN1OctetString.getInstance().encode(extnValue);
        }
        return rawExtnValue;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = Extension.ASN1.encode(this);
        }
        return encoding;
    }
    public boolean equals(Object ext) {
        if (!(ext instanceof Extension)) {
            return false;
        }
        Extension extn = (Extension) ext;
        return Arrays.equals(extnID, extn.extnID) 
            && (critical == extn.critical)
            && Arrays.equals(extnValue, extn.extnValue);
    }
    public int hashCode() {
    	return (extnID.hashCode() * 37 + (critical ? 1 : 0)) * 37 + extnValue.hashCode();
    }
    public ExtensionValue getDecodedExtensionValue() throws IOException {
        if (!valueDecoded) {
            decodeExtensionValue();
        }
        return extnValueObject;
    }
    public KeyUsage getKeyUsageValue() {
        if (!valueDecoded) {
            try {
                decodeExtensionValue();
            } catch (IOException e) { }
        }
        if (extnValueObject instanceof KeyUsage) {
            return (KeyUsage) extnValueObject;
        } else {
            return null;
        }
    }
    public BasicConstraints getBasicConstraintsValue() {
        if (!valueDecoded) {
            try {
                decodeExtensionValue();
            } catch (IOException e) { }
        }
        if (extnValueObject instanceof BasicConstraints) {
            return (BasicConstraints) extnValueObject;
        } else {
            return null;
        }
    }
    private void decodeExtensionValue() throws IOException {
        if (valueDecoded) {
            return;
        }
        valueDecoded = true;
        if (oidEquals(extnID, SUBJ_KEY_ID)) {
            extnValueObject = SubjectKeyIdentifier.decode(extnValue);
        } else if (oidEquals(extnID, KEY_USAGE)) {
            extnValueObject = new KeyUsage(extnValue);
        } else if (oidEquals(extnID, SUBJECT_ALT_NAME)) {
            extnValueObject = new AlternativeName(
                    AlternativeName.SUBJECT, extnValue);
        } else if (oidEquals(extnID, ISSUER_ALTERNATIVE_NAME)) {
            extnValueObject = new AlternativeName(
                    AlternativeName.SUBJECT, extnValue);
        } else if (oidEquals(extnID, BASIC_CONSTRAINTS)) {
            extnValueObject = new BasicConstraints(extnValue);
        } else if (oidEquals(extnID, NAME_CONSTRAINTS)) {
            extnValueObject = NameConstraints.decode(extnValue);
        } else if (oidEquals(extnID, CERTIFICATE_POLICIES)) {
            extnValueObject = CertificatePolicies.decode(extnValue);
        } else if (oidEquals(extnID, AUTH_KEY_ID)) {
            extnValueObject = AuthorityKeyIdentifier.decode(extnValue);
        } else if (oidEquals(extnID, POLICY_CONSTRAINTS)) {
            extnValueObject = new PolicyConstraints(extnValue);
        } else if (oidEquals(extnID, EXTENDED_KEY_USAGE)) {
            extnValueObject = new ExtendedKeyUsage(extnValue);
        } else if (oidEquals(extnID, INHIBIT_ANY_POLICY)) {
            extnValueObject = new InhibitAnyPolicy(extnValue);
        } else if (oidEquals(extnID, CERTIFICATE_ISSUER)) {
            extnValueObject = new CertificateIssuer(extnValue);
        } else if (oidEquals(extnID, CRL_DISTR_POINTS)) {
            extnValueObject = CRLDistributionPoints.decode(extnValue);
        } else if (oidEquals(extnID, CERTIFICATE_ISSUER)) {
            extnValueObject = new ReasonCode(extnValue);
        } else if (oidEquals(extnID, INVALIDITY_DATE)) {
            extnValueObject = new InvalidityDate(extnValue);
        } else if (oidEquals(extnID, REASON_CODE)) {
            extnValueObject = new ReasonCode(extnValue);
        } else if (oidEquals(extnID, CRL_NUMBER)) {
            extnValueObject = new CRLNumber(extnValue);
        } else if (oidEquals(extnID, ISSUING_DISTR_POINTS)) {
            extnValueObject = IssuingDistributionPoint.decode(extnValue);
        } else if (oidEquals(extnID, AUTHORITY_INFO_ACCESS)) {
            extnValueObject = InfoAccessSyntax.decode(extnValue);
        } else if (oidEquals(extnID, SUBJECT_INFO_ACCESS)) {
            extnValueObject = InfoAccessSyntax.decode(extnValue);
        }
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append("OID: ").append(getExtnID()) 
            .append(", Critical: ").append(critical).append('\n'); 
        if (!valueDecoded) {
            try {
                decodeExtensionValue();
            } catch (IOException e) { }
        }
        if (extnValueObject != null) {
            extnValueObject.dumpValue(buffer, prefix);
            return;
        }
        buffer.append(prefix);
        if (oidEquals(extnID, SUBJ_DIRECTORY_ATTRS)) {
            buffer.append("Subject Directory Attributes Extension"); 
        } else if (oidEquals(extnID, SUBJ_KEY_ID)) {
            buffer.append("Subject Key Identifier Extension"); 
        } else if (oidEquals(extnID, KEY_USAGE)) {
            buffer.append("Key Usage Extension"); 
        } else if (oidEquals(extnID, PRIVATE_KEY_USAGE_PERIOD)) {
            buffer.append("Private Key Usage Period Extension"); 
        } else if (oidEquals(extnID, SUBJECT_ALT_NAME)) {
            buffer.append("Subject Alternative Name Extension"); 
        } else if (oidEquals(extnID, ISSUER_ALTERNATIVE_NAME)) {
            buffer.append("Issuer Alternative Name Extension"); 
        } else if (oidEquals(extnID, BASIC_CONSTRAINTS)) {
            buffer.append("Basic Constraints Extension"); 
        } else if (oidEquals(extnID, NAME_CONSTRAINTS)) {
            buffer.append("Name Constraints Extension"); 
        } else if (oidEquals(extnID, CRL_DISTR_POINTS)) {
            buffer.append("CRL Distribution Points Extension"); 
        } else if (oidEquals(extnID, CERTIFICATE_POLICIES)) {
            buffer.append("Certificate Policies Extension"); 
        } else if (oidEquals(extnID, POLICY_MAPPINGS)) {
            buffer.append("Policy Mappings Extension"); 
        } else if (oidEquals(extnID, AUTH_KEY_ID)) {
            buffer.append("Authority Key Identifier Extension"); 
        } else if (oidEquals(extnID, POLICY_CONSTRAINTS)) {
            buffer.append("Policy Constraints Extension"); 
        } else if (oidEquals(extnID, EXTENDED_KEY_USAGE)) {
            buffer.append("Extended Key Usage Extension"); 
        } else if (oidEquals(extnID, INHIBIT_ANY_POLICY)) {
            buffer.append("Inhibit Any-Policy Extension"); 
        } else if (oidEquals(extnID, AUTHORITY_INFO_ACCESS)) {
            buffer.append("Authority Information Access Extension"); 
        } else if (oidEquals(extnID, SUBJECT_INFO_ACCESS)) {
            buffer.append("Subject Information Access Extension"); 
        } else if (oidEquals(extnID, INVALIDITY_DATE)) {
            buffer.append("Invalidity Date Extension"); 
        } else if (oidEquals(extnID, CRL_NUMBER)) {
            buffer.append("CRL Number Extension"); 
        } else if (oidEquals(extnID, REASON_CODE)) {
            buffer.append("Reason Code Extension"); 
        } else {
            buffer.append("Unknown Extension"); 
        }
        buffer.append('\n').append(prefix)
            .append("Unparsed Extension Value:\n"); 
        buffer.append(Array.toString(extnValue, prefix));
    }
    private static boolean oidEquals(int[] oid1, int[] oid2) {
        int length = oid1.length;
        if (length != oid2.length) {
            return false;
        }
        while (length > 0) {
            if (oid1[--length] != oid2[length]) {
                return false;
            }
        }
        return true;
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            ASN1Oid.getInstance(), 
            ASN1Boolean.getInstance(),
            new ASN1OctetString() {
                public Object getDecodedObject(BerInputStream in) 
                                                throws IOException {
                    return new Object[] 
                        {super.getDecodedObject(in), in.getEncoded()};
                }
            }
        }) {
        {
            setDefault(Boolean.FALSE, 1);
        }
        protected Object getDecodedObject(BerInputStream in) throws IOException {
            Object[] values = (Object[]) in.content;
            int[] oid = (int[]) values[0];
            byte[] extnValue = (byte[]) ((Object[]) values[2])[0];
            byte[] rawExtnValue = (byte[]) ((Object[]) values[2])[1];
            ExtensionValue decodedExtValue = null;
            if (oidEquals(oid, KEY_USAGE)) {
                decodedExtValue = new KeyUsage(extnValue);
            } else if (oidEquals(oid, BASIC_CONSTRAINTS)) {
                decodedExtValue = new BasicConstraints(extnValue);
            }
            return 
                new Extension((int[]) values[0],
                    ((Boolean) values[1]).booleanValue(),
                    extnValue, rawExtnValue, in.getEncoded(), decodedExtValue);
        }
        protected void getValues(Object object, Object[] values) {
            Extension ext = (Extension) object;
            values[0] = ext.extnID;
            values[1] = (ext.critical) ? Boolean.TRUE : Boolean.FALSE;
            values[2] = ext.extnValue;
        }
    };
}
