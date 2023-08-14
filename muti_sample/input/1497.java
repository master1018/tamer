public final class RSAPrivateCrtKeyImpl
        extends PKCS8Key implements RSAPrivateCrtKey {
    private static final long serialVersionUID = -1326088454257084918L;
    private BigInteger n;       
    private BigInteger e;       
    private BigInteger d;       
    private BigInteger p;       
    private BigInteger q;       
    private BigInteger pe;      
    private BigInteger qe;      
    private BigInteger coeff;   
    final static AlgorithmId rsaId =
        new AlgorithmId(AlgorithmId.RSAEncryption_oid);
    public static RSAPrivateKey newKey(byte[] encoded)
            throws InvalidKeyException {
        RSAPrivateCrtKeyImpl key = new RSAPrivateCrtKeyImpl(encoded);
        if (key.getPublicExponent().signum() == 0) {
            return new RSAPrivateKeyImpl(
                key.getModulus(),
                key.getPrivateExponent()
            );
        } else {
            return key;
        }
    }
    RSAPrivateCrtKeyImpl(byte[] encoded) throws InvalidKeyException {
        decode(encoded);
        RSAKeyFactory.checkRSAProviderKeyLengths(n.bitLength(), e);
    }
    RSAPrivateCrtKeyImpl(BigInteger n, BigInteger e, BigInteger d,
            BigInteger p, BigInteger q, BigInteger pe, BigInteger qe,
            BigInteger coeff) throws InvalidKeyException {
        this.n = n;
        this.e = e;
        this.d = d;
        this.p = p;
        this.q = q;
        this.pe = pe;
        this.qe = qe;
        this.coeff = coeff;
        RSAKeyFactory.checkRSAProviderKeyLengths(n.bitLength(), e);
        algid = rsaId;
        try {
            DerOutputStream out = new DerOutputStream();
            out.putInteger(0); 
            out.putInteger(n);
            out.putInteger(e);
            out.putInteger(d);
            out.putInteger(p);
            out.putInteger(q);
            out.putInteger(pe);
            out.putInteger(qe);
            out.putInteger(coeff);
            DerValue val =
                new DerValue(DerValue.tag_Sequence, out.toByteArray());
            key = val.toByteArray();
        } catch (IOException exc) {
            throw new InvalidKeyException(exc);
        }
    }
    public String getAlgorithm() {
        return "RSA";
    }
    public BigInteger getModulus() {
        return n;
    }
    public BigInteger getPublicExponent() {
        return e;
    }
    public BigInteger getPrivateExponent() {
        return d;
    }
    public BigInteger getPrimeP() {
        return p;
    }
    public BigInteger getPrimeQ() {
        return q;
    }
    public BigInteger getPrimeExponentP() {
        return pe;
    }
    public BigInteger getPrimeExponentQ() {
        return qe;
    }
    public BigInteger getCrtCoefficient() {
        return coeff;
    }
    protected void parseKeyBits() throws InvalidKeyException {
        try {
            DerInputStream in = new DerInputStream(key);
            DerValue derValue = in.getDerValue();
            if (derValue.tag != DerValue.tag_Sequence) {
                throw new IOException("Not a SEQUENCE");
            }
            DerInputStream data = derValue.data;
            int version = data.getInteger();
            if (version != 0) {
                throw new IOException("Version must be 0");
            }
            n = getBigInteger(data);
            e = getBigInteger(data);
            d = getBigInteger(data);
            p = getBigInteger(data);
            q = getBigInteger(data);
            pe = getBigInteger(data);
            qe = getBigInteger(data);
            coeff = getBigInteger(data);
            if (derValue.data.available() != 0) {
                throw new IOException("Extra data available");
            }
        } catch (IOException e) {
            throw new InvalidKeyException("Invalid RSA private key", e);
        }
    }
    static BigInteger getBigInteger(DerInputStream data) throws IOException {
        BigInteger b = data.getBigInteger();
        if (b.signum() < 0) {
            b = new BigInteger(1, b.toByteArray());
        }
        return b;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Sun RSA private CRT key, ");
        sb.append(n.bitLength());
        sb.append(" bits\n  modulus:          ");
        sb.append(n);
        sb.append("\n  public exponent:  ");
        sb.append(e);
        sb.append("\n  private exponent: ");
        sb.append(d);
        sb.append("\n  prime p:          ");
        sb.append(p);
        sb.append("\n  prime q:          ");
        sb.append(q);
        sb.append("\n  prime exponent p: ");
        sb.append(pe);
        sb.append("\n  prime exponent q: ");
        sb.append(qe);
        sb.append("\n  crt coefficient:  ");
        sb.append(coeff);
        return sb.toString();
    }
}
