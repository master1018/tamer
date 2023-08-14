public final class RSAPublicKeyImpl extends X509Key implements RSAPublicKey {
    private static final long serialVersionUID = 2644735423591199609L;
    private BigInteger n;       
    private BigInteger e;       
    public RSAPublicKeyImpl(BigInteger n, BigInteger e)
            throws InvalidKeyException {
        this.n = n;
        this.e = e;
        RSAKeyFactory.checkRSAProviderKeyLengths(n.bitLength(), e);
        algid = RSAPrivateCrtKeyImpl.rsaId;
        try {
            DerOutputStream out = new DerOutputStream();
            out.putInteger(n);
            out.putInteger(e);
            DerValue val =
                new DerValue(DerValue.tag_Sequence, out.toByteArray());
            key = val.toByteArray();
        } catch (IOException exc) {
            throw new InvalidKeyException(exc);
        }
    }
    public RSAPublicKeyImpl(byte[] encoded) throws InvalidKeyException {
        decode(encoded);
        RSAKeyFactory.checkRSAProviderKeyLengths(n.bitLength(), e);
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
    protected void parseKeyBits() throws InvalidKeyException {
        try {
            DerInputStream in = new DerInputStream(key);
            DerValue derValue = in.getDerValue();
            if (derValue.tag != DerValue.tag_Sequence) {
                throw new IOException("Not a SEQUENCE");
            }
            DerInputStream data = derValue.data;
            n = RSAPrivateCrtKeyImpl.getBigInteger(data);
            e = RSAPrivateCrtKeyImpl.getBigInteger(data);
            if (derValue.data.available() != 0) {
                throw new IOException("Extra data available");
            }
        } catch (IOException e) {
            throw new InvalidKeyException("Invalid RSA public key", e);
        }
    }
    public String toString() {
        return "Sun RSA public key, " + n.bitLength() + " bits\n  modulus: "
                + n + "\n  public exponent: " + e;
    }
    protected Object writeReplace() throws java.io.ObjectStreamException {
        return new KeyRep(KeyRep.Type.PUBLIC,
                        getAlgorithm(),
                        getFormat(),
                        getEncoded());
    }
}
