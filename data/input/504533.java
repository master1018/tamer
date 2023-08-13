public class SubjectPublicKeyInfo {
    private AlgorithmIdentifier algorithmID;
    private byte[] subjectPublicKey;
    private PublicKey publicKey;
    private int unusedBits;
    private byte[] encoding;
    public SubjectPublicKeyInfo(AlgorithmIdentifier algID, 
                                byte[] subjectPublicKey) { 
        this(algID, subjectPublicKey, 0);
    }
    public SubjectPublicKeyInfo(AlgorithmIdentifier algID, 
                                byte[] subjectPublicKey, int unused) {
        this(algID, subjectPublicKey, 0, null);
    }
    private SubjectPublicKeyInfo(AlgorithmIdentifier algID, 
                                 byte[] subjectPublicKey, int unused, 
                                 byte[] encoding) {
        this.algorithmID = algID;
        this.subjectPublicKey = subjectPublicKey;
        this.unusedBits = unused;
        this.encoding = encoding;
    }
    public AlgorithmIdentifier getAlgorithmIdentifier() {
        return algorithmID;
    }
    public byte[] getSubjectPublicKey() {
        return subjectPublicKey;
    }
    public int getUnusedBits() {
        return unusedBits;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public PublicKey getPublicKey() {
        if (publicKey == null) {
            String alg_oid = algorithmID.getAlgorithm();
            try {
                String alg = 
                    AlgNameMapper.map2AlgName(alg_oid);
                if (alg == null) {
                    alg = alg_oid;
                }
                publicKey = KeyFactory.getInstance(alg)
                    .generatePublic(new X509EncodedKeySpec(getEncoded()));
            } catch (InvalidKeySpecException e) {
            } catch (NoSuchAlgorithmException e) {
            }
            if (publicKey == null) {
                publicKey = new X509PublicKey(alg_oid, getEncoded(),
                        subjectPublicKey);
            }
        }
        return publicKey;
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            AlgorithmIdentifier.ASN1, ASN1BitString.getInstance() }) {
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new SubjectPublicKeyInfo(
                    (AlgorithmIdentifier) values[0],
                    ((BitString) values[1]).bytes,
                    ((BitString) values[1]).unusedBits,
                    in.getEncoded());
        }
        protected void getValues(Object object, Object[] values) {
            SubjectPublicKeyInfo spki = (SubjectPublicKeyInfo) object;
            values[0] = spki.algorithmID;
            values[1] = new BitString(spki.subjectPublicKey, spki.unusedBits);
        }
    };
}
