public class TBSCertificate {
    private final int version; 
    private final BigInteger serialNumber;
    private final AlgorithmIdentifier signature; 
    private final Name issuer;
    private final Validity validity;
    private final Name subject;
    private final SubjectPublicKeyInfo subjectPublicKeyInfo;
    private final boolean[] issuerUniqueID;
    private final boolean[] subjectUniqueID;
    private final Extensions extensions;
    byte[] encoding;
    public TBSCertificate(int version, BigInteger serialNumber, 
                          AlgorithmIdentifier signature, Name issuer,
                          Validity validity, Name subject, 
                          SubjectPublicKeyInfo subjectPublicKeyInfo) {
        this(version, serialNumber, signature, issuer, validity, subject, 
             subjectPublicKeyInfo, null, null, null);
    }
    public TBSCertificate(int version, BigInteger serialNumber, 
                          AlgorithmIdentifier signature, Name issuer,
                          Validity validity, Name subject, 
                          SubjectPublicKeyInfo subjectPublicKeyInfo,
                          boolean[] issuerUniqueID, boolean[] subjectUniqueID,
                          Extensions extensions) {
        this.version = version; 
        this.serialNumber = serialNumber;
        this.signature = signature; 
        this.issuer = issuer;
        this.validity = validity;
        this.subject = subject;
        this.subjectPublicKeyInfo = subjectPublicKeyInfo;
        this.issuerUniqueID = issuerUniqueID;
        this.subjectUniqueID = subjectUniqueID;
        this.extensions = extensions;
    }
    private TBSCertificate(int version, BigInteger serialNumber, 
                          AlgorithmIdentifier signature, Name issuer,
                          Validity validity, Name subject, 
                          SubjectPublicKeyInfo subjectPublicKeyInfo,
                          boolean[] issuerUniqueID, boolean[] subjectUniqueID,
                          Extensions extensions, byte[] encoding) {
        this(version, serialNumber, signature, issuer, validity, subject, 
             subjectPublicKeyInfo, issuerUniqueID, subjectUniqueID, extensions);
        this.encoding = encoding;
    }
    public int getVersion() {
        return version;
    }
    public BigInteger getSerialNumber() {
        return serialNumber;
    }
    public AlgorithmIdentifier getSignature() {
        return signature;
    }
    public Name getIssuer() {
        return issuer;
    }
    public Validity getValidity() {
        return validity;
    }
    public Name getSubject() {
        return subject;
    }
    public SubjectPublicKeyInfo getSubjectPublicKeyInfo() {
        return subjectPublicKeyInfo;
    }
    public boolean[] getIssuerUniqueID() {
        return issuerUniqueID;
    }
    public boolean[] getSubjectUniqueID() {
        return subjectUniqueID;
    }
    public Extensions getExtensions() {
        return extensions;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public void dumpValue(StringBuffer buffer) {
        buffer.append('[');
        buffer.append("\n  Version: V").append(version+1); 
        buffer.append("\n  Subject: ") 
            .append(subject.getName(X500Principal.RFC2253));
        buffer.append("\n  Signature Algorithm: "); 
        signature.dumpValue(buffer);
        buffer.append("\n  Key: "); 
        buffer.append(subjectPublicKeyInfo.getPublicKey().toString());
        buffer.append("\n  Validity: [From: "); 
        buffer.append(validity.getNotBefore());
        buffer.append("\n               To: "); 
        buffer.append(validity.getNotAfter()).append(']');
        buffer.append("\n  Issuer: "); 
        buffer.append(issuer.getName(X500Principal.RFC2253));
        buffer.append("\n  Serial Number: "); 
        buffer.append(serialNumber);
        if (issuerUniqueID != null) {
            buffer.append("\n  Issuer Id: "); 
            for (int i=0; i<issuerUniqueID.length; i++) {
                buffer.append(issuerUniqueID[i] ? '1' : '0');
            }
        }
        if (subjectUniqueID != null) {
            buffer.append("\n  Subject Id: "); 
            for (int i=0; i<subjectUniqueID.length; i++) {
                buffer.append(subjectUniqueID[i] ? '1' : '0');
            }
        }
        if (extensions != null) {
            buffer.append("\n\n  Extensions: "); 
            buffer.append("[\n"); 
            extensions.dumpValue(buffer, "    "); 
            buffer.append("  ]"); 
        }
        buffer.append("\n]"); 
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            new ASN1Explicit(0, ASN1Integer.getInstance()), ASN1Integer.getInstance(), 
            AlgorithmIdentifier.ASN1, Name.ASN1,
            Validity.ASN1, Name.ASN1, SubjectPublicKeyInfo.ASN1, 
            new ASN1Implicit(1, ASN1BitString.getInstance()), 
            new ASN1Implicit(2, ASN1BitString.getInstance()), 
            new ASN1Explicit(3, Extensions.ASN1)}) {
        {
            setDefault(new byte[] {0}, 0);
            setOptional(7);
            setOptional(8);
            setOptional(9);
        }
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            boolean[] issuerUniqueID = (values[7] == null) 
                ? null : ((BitString) values[7]).toBooleanArray();
            boolean[] subjectUniqueID = (values[8] == null) 
                ? null : ((BitString) values[8]).toBooleanArray();
            return new TBSCertificate(
                        ASN1Integer.toIntValue(values[0]),
                        new BigInteger((byte[]) values[1]), 
                        (AlgorithmIdentifier) values[2],
                        (Name) values[3], 
                        (Validity) values[4], 
                        (Name) values[5],
                        (SubjectPublicKeyInfo) values[6], 
                        issuerUniqueID,
                        subjectUniqueID,
                        (Extensions) values[9],
                        in.getEncoded()
                    );
        }
        protected void getValues(Object object, Object[] values) {
            TBSCertificate tbs = (TBSCertificate) object;
            values[0] = ASN1Integer.fromIntValue(tbs.version);
            values[1] = tbs.serialNumber.toByteArray();
            values[2] = tbs.signature;
            values[3] = tbs.issuer; 
            values[4] = tbs.validity;
            values[5] = tbs.subject;
            values[6] = tbs.subjectPublicKeyInfo;
            if (tbs.issuerUniqueID != null) {
                values[7] = new BitString(tbs.issuerUniqueID);
            }
            if (tbs.subjectUniqueID != null) {
                values[8] = new BitString(tbs.subjectUniqueID);
            }
            values[9] = tbs.extensions;
        }
    };
}
