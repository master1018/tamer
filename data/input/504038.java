public class DSAPrivateKeyImpl extends PrivateKeyImpl implements DSAPrivateKey {
    private static final long serialVersionUID = -4716227614104950081L;
    private BigInteger x, g, p, q;
    private transient DSAParams params;
    public DSAPrivateKeyImpl(DSAPrivateKeySpec keySpec) {
        super("DSA"); 
        PrivateKeyInfo pki;
        g = keySpec.getG();
        p = keySpec.getP();
        q = keySpec.getQ();
        ThreeIntegerSequence threeInts = new ThreeIntegerSequence(p
                .toByteArray(), q.toByteArray(), g.toByteArray());
        AlgorithmIdentifier ai = new AlgorithmIdentifier(AlgNameMapper
                .map2OID("DSA"), 
                threeInts.getEncoded());
        x = keySpec.getX();
        pki = new PrivateKeyInfo(0, ai, ASN1Integer.getInstance().encode(
                x.toByteArray()), null);
        setEncoding(pki.getEncoded());
        params = new DSAParameterSpec(p, q, g);
    }
    public DSAPrivateKeyImpl(PKCS8EncodedKeySpec keySpec)
            throws InvalidKeySpecException {
        super("DSA"); 
        AlgorithmIdentifier ai;
        ThreeIntegerSequence threeInts = null;
        String alg, algName;
        byte encoding[] = keySpec.getEncoded();
        PrivateKeyInfo privateKeyInfo = null;
        try {
            privateKeyInfo = (PrivateKeyInfo) PrivateKeyInfo.ASN1
                    .decode(encoding);
        } catch (IOException e) {
            throw new InvalidKeySpecException(Messages.getString(
                    "security.19A", e)); 
        }
        try {
            x = new BigInteger((byte[]) ASN1Integer.getInstance().decode(
                    privateKeyInfo.getPrivateKey()));
        } catch (IOException e) {
            throw new InvalidKeySpecException(Messages.getString(
                    "security.19B", e)); 
        }
        ai = privateKeyInfo.getAlgorithmIdentifier();
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
        params = new DSAParameterSpec(p, q, g);
        setEncoding(encoding);
        alg = ai.getAlgorithm();
        algName = AlgNameMapper.map2AlgName(alg);
        setAlgorithm(algName == null ? alg : algName);
    }
    public BigInteger getX() {
        return x;
    }
    public DSAParams getParams() {
        return params;
    }
    private void readObject(java.io.ObjectInputStream in) throws NotActiveException, IOException, ClassNotFoundException {
    	in.defaultReadObject();
    	params = new DSAParameterSpec(p, q, g);    	
    }
}
