public final class RSAPrivateKeyImpl extends PKCS8Key implements RSAPrivateKey {
    private static final long serialVersionUID = -33106691987952810L;
    private final BigInteger n;         
    private final BigInteger d;         
    RSAPrivateKeyImpl(BigInteger n, BigInteger d) throws InvalidKeyException {
        this.n = n;
        this.d = d;
        RSAKeyFactory.checkRSAProviderKeyLengths(n.bitLength(), null);
        algid = RSAPrivateCrtKeyImpl.rsaId;
        try {
            DerOutputStream out = new DerOutputStream();
            out.putInteger(0); 
            out.putInteger(n);
            out.putInteger(0);
            out.putInteger(d);
            out.putInteger(0);
            out.putInteger(0);
            out.putInteger(0);
            out.putInteger(0);
            out.putInteger(0);
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
    public BigInteger getPrivateExponent() {
        return d;
    }
    public String toString() {
        return "Sun RSA private key, " + n.bitLength() + " bits\n  modulus: "
                + n + "\n  private exponent: " + d;
    }
}
