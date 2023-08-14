public class CertificationRequestInfo {
    private int version;
    private Name subject;
    private SubjectPublicKeyInfo subjectPublicKeyInfo;
    private List attributes;
    private byte [] encoding;
    public CertificationRequestInfo(int version, Name subject,
            SubjectPublicKeyInfo subjectPublicKeyInfo, List attributes) {
        this.version = version;
        this.subject = subject;
        this.subjectPublicKeyInfo = subjectPublicKeyInfo;
        this.attributes = attributes;
    }
    private CertificationRequestInfo(int version, Name subject,
            SubjectPublicKeyInfo subjectPublicKeyInfo, List attributes, byte [] encoding) {
        this(version, subject, subjectPublicKeyInfo, attributes);
        this.encoding = encoding;
    }
    public List getAttributes() {
        return attributes;
    }
    public Name getSubject() {
        return subject;
    }
    public SubjectPublicKeyInfo getSubjectPublicKeyInfo() {
        return subjectPublicKeyInfo;
    }
    public int getVersion() {
        return version;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("-- CertificationRequestInfo:"); 
        res.append("\n version: "); 
        res.append(version);
        res.append("\n subject: "); 
        res.append(subject.getName(X500Principal.CANONICAL));
        res.append("\n subjectPublicKeyInfo: "); 
        res.append("\n\t algorithm: " 
                + subjectPublicKeyInfo.getAlgorithmIdentifier().getAlgorithm());
        res.append("\n\t public key: " + subjectPublicKeyInfo.getPublicKey()); 
        res.append("\n attributes: "); 
        if (attributes != null) {
            res.append(attributes.toString());
        } else {
            res.append("none"); 
        }
        res.append("\n-- CertificationRequestInfo End\n"); 
        return res.toString();
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            ASN1Integer.getInstance(),              
            Name.ASN1,                              
            SubjectPublicKeyInfo.ASN1,              
            new ASN1Implicit(0, new ASN1SetOf(
                    AttributeTypeAndValue.ASN1))    
            }) {
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new CertificationRequestInfo(
                    ASN1Integer.toIntValue(values[0]),
                    (Name) values[1],
                    (SubjectPublicKeyInfo) values[2],
                    (List) values[3],
                    in.getEncoded());
        }
        protected void getValues(Object object, Object[] values) {
            CertificationRequestInfo certReqInfo = (CertificationRequestInfo) object;
            values[0] = ASN1Integer.fromIntValue(certReqInfo.version);
            values[1] = certReqInfo.subject;
            values[2] = certReqInfo.subjectPublicKeyInfo;
            values[3] = certReqInfo.attributes;
        }
    };
}
