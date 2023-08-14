public class AlgorithmIdentifier {
    private String algorithm;
    private String algorithmName;
    private byte[] parameters;
    private byte[] encoding;
    public AlgorithmIdentifier(String algorithm) {
        this(algorithm, null, null);
    }
    public AlgorithmIdentifier(String algorithm, byte[] parameters) {
        this(algorithm, parameters, null);
    }
    private AlgorithmIdentifier(String algorithm, byte[] parameters, 
                                byte[] encoding) {
        this.algorithm = algorithm;
        this.parameters = parameters;
        this.encoding = encoding;
    }
    public String getAlgorithm() {
        return algorithm;
    }
    public String getAlgorithmName() {
        if (algorithmName == null) {
            algorithmName = AlgNameMapper.map2AlgName(algorithm);
            if (algorithmName == null) {
                algorithmName = algorithm;
            }
        }
        return algorithmName;
    }
    public byte[] getParameters() {
        return parameters;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public boolean equals(Object ai) {
        if (!(ai instanceof AlgorithmIdentifier)) {
            return false;
        }
        AlgorithmIdentifier algid = (AlgorithmIdentifier) ai;
        return (algorithm.equals(algid.algorithm))
            && ((parameters == null)
                    ? algid.parameters == null
                    : Arrays.equals(parameters, algid.parameters));
    }
    public int hashCode() {
    	return algorithm.hashCode() * 37 + 
    		(parameters != null ? parameters.hashCode() : 0);
    }
    public void dumpValue(StringBuffer buffer) {
        buffer.append(getAlgorithmName());
        if (parameters == null) {
            buffer.append(", no params, "); 
        } else {
            buffer.append(", params unparsed, "); 
        }
        buffer.append("OID = "); 
        buffer.append(getAlgorithm());
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            ASN1Oid.getInstance(), ASN1Any.getInstance() }) {
        {
            setOptional(1); 
        }
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new AlgorithmIdentifier(ObjectIdentifier
                    .toString((int[]) values[0]), (byte[]) values[1]);
        }
        protected void getValues(Object object, Object[] values) {
            AlgorithmIdentifier aID = (AlgorithmIdentifier) object;
            values[0] = ObjectIdentifier.toIntArray(aID.getAlgorithm());
            values[1] = aID.getParameters();
        }
    };
}
