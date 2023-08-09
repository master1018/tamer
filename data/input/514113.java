public class CertificationRequest {
    private CertificationRequestInfo info;
    private AlgorithmIdentifier algId;
    private byte[] signature;
    private byte[] encoding;
    public CertificationRequest(CertificationRequestInfo info,
            AlgorithmIdentifier algId, byte[] signature) {
        this.info = info;
        this.algId = algId;
        this.signature = new byte[signature.length];
        System.arraycopy(signature, 0, this.signature, 0, signature.length);
    }
    private CertificationRequest(CertificationRequestInfo info,
            AlgorithmIdentifier algId, byte[] signature, byte[] encoding) {
        this(info, algId, signature);
        this.encoding = encoding;
    }
    public AlgorithmIdentifier getAlgId() {
        return algId;
    }
    public CertificationRequestInfo getInfo() {
        return info;
    }
    public byte[] getSignature() {
        byte[] result = new byte[signature.length];
        System.arraycopy(signature, 0, result, 0, signature.length);
        return result;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = CertificationRequest.ASN1.encode(this);
        }
        return encoding;
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            CertificationRequestInfo.ASN1,  
            AlgorithmIdentifier.ASN1,       
            ASN1BitString.getInstance() })  
    {
        public Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new CertificationRequest(
                    (CertificationRequestInfo) values[0],
                    (AlgorithmIdentifier) values[1],
                    ((BitString) values[2]).bytes, 
                    in.getEncoded());
        }
        protected void getValues(Object object, Object[] values) {
            CertificationRequest certReq = (CertificationRequest) object;
            values[0] = certReq.info;
            values[1] = certReq.algId;
            values[2] = new BitString(certReq.signature, 0);
        }
    };
}
