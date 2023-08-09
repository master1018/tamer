public class CertificateList {
    private final TBSCertList tbsCertList; 
    private final AlgorithmIdentifier signatureAlgorithm;
    private final byte[] signatureValue;
    private byte[] encoding;
    public CertificateList(TBSCertList tbsCertList, 
                       AlgorithmIdentifier signatureAlgorithm,
                       byte[] signatureValue) {
        this.tbsCertList = tbsCertList;
        this.signatureAlgorithm = signatureAlgorithm;
        this.signatureValue = new byte[signatureValue.length];
        System.arraycopy(signatureValue, 0, this.signatureValue, 0, 
                                                    signatureValue.length);
    }
    private CertificateList(TBSCertList tbsCertList, 
                       AlgorithmIdentifier signatureAlgorithm,
                       byte[] signatureValue, byte[] encoding) {
        this(tbsCertList, signatureAlgorithm, signatureValue);
        this.encoding = encoding;
    }
    public TBSCertList getTbsCertList() {
        return tbsCertList;
    }
    public AlgorithmIdentifier getSignatureAlgorithm() {
        return signatureAlgorithm;
    }
    public byte[] getSignatureValue() {
        byte[] result = new byte[signatureValue.length];
        System.arraycopy(signatureValue, 0, result, 0, signatureValue.length);
        return result;
    }
    public String toString() {
        StringBuffer res = new StringBuffer();
        tbsCertList.dumpValue(res);
        res.append("\nSignature Value:\n"); 
        res.append(Array.toString(signatureValue, "")); 
        return res.toString();
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = CertificateList.ASN1.encode(this);
        }
        return encoding;
    }
    public static final ASN1Sequence ASN1 = 
        new ASN1Sequence(new ASN1Type[] 
                {TBSCertList.ASN1, AlgorithmIdentifier.ASN1, 
                    ASN1BitString.getInstance()}) {
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new CertificateList(
                    (TBSCertList) values[0],
                    (AlgorithmIdentifier) values[1], 
                    ((BitString) values[2]).bytes, 
                    in.getEncoded()
                    );
        }
        protected void getValues(Object object, Object[] values) {
            CertificateList certlist = (CertificateList) object;
            values[0] = certlist.tbsCertList;
            values[1] = certlist.signatureAlgorithm;
            values[2] = new BitString(certlist.signatureValue, 0);
        }
    };
}
