public final class DSAPublicKeyImpl extends DSAPublicKey {
    private static final long serialVersionUID = 7819830118247182730L;
    public DSAPublicKeyImpl(BigInteger y, BigInteger p, BigInteger q,
                        BigInteger g)
                throws InvalidKeyException {
        super(y, p, q, g);
    }
    public DSAPublicKeyImpl(byte[] encoded) throws InvalidKeyException {
        super(encoded);
    }
    protected Object writeReplace() throws java.io.ObjectStreamException {
        return new KeyRep(KeyRep.Type.PUBLIC,
                        getAlgorithm(),
                        getFormat(),
                        getEncoded());
    }
}
