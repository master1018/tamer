public class DSAPublicKeyImpl extends PublicKeyImpl implements DSAPublicKey {
    private static final long serialVersionUID = -2279672131310978336L;
    private BigInteger y, g, p, q;
    private transient DSAParams params;
    public DSAPublicKeyImpl(DSAPublicKeySpec keySpec) {
        super("DSA"); 
        SubjectPublicKeyInfo spki;
        p = keySpec.getP();
        q = keySpec.getQ();
        g = keySpec.getG();
        ThreeIntegerSequence threeInts = new ThreeIntegerSequence(p
                .toByteArray(), q.toByteArray(), g.toByteArray());
        AlgorithmIdentifier ai = new AlgorithmIdentifier(AlgNameMapper
                .map2OID("DSA"), 
                threeInts.getEncoded());
        y = keySpec.getY();
        spki = new SubjectPublicKeyInfo(ai, ASN1Integer.getInstance().encode(
                y.toByteArray()));
        setEncoding(spki.getEncoded());
        params = (DSAParams) (new DSAParameterSpec(p, q, g));
    }
    public DSAPublicKeyImpl(X509EncodedKeySpec keySpec)
            throws InvalidKeySpecException {
        super("DSA"); 
        AlgorithmIdentifier ai;
        ThreeIntegerSequence threeInts = null;
        SubjectPublicKeyInfo subjectPublicKeyInfo = null;
        byte encoding[] = keySpec.getEncoded();
        String alg, algName;
        try {
            subjectPublicKeyInfo = (SubjectPublicKeyInfo) SubjectPublicKeyInfo.ASN1
                    .decode(encoding);
        } catch (IOException e) {
            throw new InvalidKeySpecException(Messages.getString(
                    "security.19A", e)); 
        }
        try {
            y = new BigInteger((byte[]) ASN1Integer.getInstance().decode(
                    subjectPublicKeyInfo.getSubjectPublicKey()));
        } catch (IOException e) {
            throw new InvalidKeySpecException(Messages.getString(
                    "security.19B", e)); 
        }
        ai = subjectPublicKeyInfo.getAlgorithmIdentifier();
        try {
            threeInts = (ThreeIntegerSequence) ThreeIntegerSequence.ASN1
                    .decode(ai.getParameters());
        } catch (IOException e) {
            throw new InvalidKeySpecException(Messages.getString(
                    "security.19B", e)); 
        }
        p = new BigInteger(threeInts.p);
        q = new BigInteger(threeInts.q);
        g = new BigInteger(threeInts.g);
        params = (DSAParams) (new DSAParameterSpec(p, q, g));
        setEncoding(encoding);
        alg = ai.getAlgorithm();
        algName = AlgNameMapper.map2AlgName(alg);
        setAlgorithm(algName == null ? alg : algName);
    }
    public BigInteger getY() {
        return y;
    }
    public DSAParams getParams() {
        return params;
    }
    private void readObject(java.io.ObjectInputStream in) throws NotActiveException, IOException, ClassNotFoundException {
    	in.defaultReadObject();
    	params = new DSAParameterSpec(p, q, g);    	
    }
}
