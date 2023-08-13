public class PrivateKeyInfo {
    private int version;
    private AlgorithmIdentifier privateKeyAlgorithm;
    private byte[] privateKey;
    private List attributes;
    private byte[] encoding;
    public PrivateKeyInfo(int version, AlgorithmIdentifier privateKeyAlgorithm,
            byte[] privateKey, List attributes) {
        this.version = version;
        this.privateKeyAlgorithm = privateKeyAlgorithm;
        this.privateKey = privateKey;
        this.attributes = attributes;
    }
    private PrivateKeyInfo(int version,
            AlgorithmIdentifier privateKeyAlgorithm, byte[] privateKey,
            List attributes, byte[] encoding) {
        this(version, privateKeyAlgorithm, privateKey, attributes);
        this.encoding = encoding;
    }
    public int getVersion() {
        return version;
    }
    public AlgorithmIdentifier getAlgorithmIdentifier() {
        return privateKeyAlgorithm;
    }
    public List getAttributes() {
        return attributes;
    }
    public byte[] getPrivateKey() {
        return privateKey;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
    ASN1Integer.getInstance(), 
            AlgorithmIdentifier.ASN1, 
            ASN1OctetString.getInstance(), 
            new ASN1Implicit(0, new ASN1SetOf(AttributeTypeAndValue.ASN1)) 
            }) {
        {
            setOptional(3); 
        }
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new PrivateKeyInfo(ASN1Integer.toIntValue(values[0]),
                    (AlgorithmIdentifier) values[1], (byte[]) values[2],
                    (List) values[3], in.getEncoded());
        }
        protected void getValues(Object object, Object[] values) {
            PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) object;
            values[0] = ASN1Integer.fromIntValue(privateKeyInfo.version);
            values[1] = privateKeyInfo.privateKeyAlgorithm;
            values[2] = privateKeyInfo.privateKey;
            values[3] = privateKeyInfo.attributes;
        }
    };
}
