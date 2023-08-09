public final class ECPrivateKeyImpl extends PKCS8Key implements ECPrivateKey {
    private static final long serialVersionUID = 88695385615075129L;
    private BigInteger s;       
    private ECParameterSpec params;
    public ECPrivateKeyImpl(byte[] encoded) throws InvalidKeyException {
        decode(encoded);
    }
    public ECPrivateKeyImpl(BigInteger s, ECParameterSpec params)
            throws InvalidKeyException {
        this.s = s;
        this.params = params;
        algid = new AlgorithmId
            (AlgorithmId.EC_oid, ECParameters.getAlgorithmParameters(params));
        try {
            DerOutputStream out = new DerOutputStream();
            out.putInteger(1); 
            byte[] privBytes = ECParameters.trimZeroes(s.toByteArray());
            out.putOctetString(privBytes);
            DerValue val =
                new DerValue(DerValue.tag_Sequence, out.toByteArray());
            key = val.toByteArray();
        } catch (IOException exc) {
            throw new InvalidKeyException(exc);
        }
    }
    public String getAlgorithm() {
        return "EC";
    }
    public BigInteger getS() {
        return s;
    }
    public ECParameterSpec getParams() {
        return params;
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
            if (version != 1) {
                throw new IOException("Version must be 1");
            }
            byte[] privData = data.getOctetString();
            s = new BigInteger(1, privData);
            while (data.available() != 0) {
                DerValue value = data.getDerValue();
                if (value.isContextSpecific((byte)0)) {
                } else if (value.isContextSpecific((byte)1)) {
                } else {
                    throw new InvalidKeyException("Unexpected value: " + value);
                }
            }
            AlgorithmParameters algParams = this.algid.getParameters();
            if (algParams == null) {
                throw new InvalidKeyException("EC domain parameters must be "
                    + "encoded in the algorithm identifier");
            }
            params = algParams.getParameterSpec(ECParameterSpec.class);
        } catch (IOException e) {
            throw new InvalidKeyException("Invalid EC private key", e);
        } catch (InvalidParameterSpecException e) {
            throw new InvalidKeyException("Invalid EC private key", e);
        }
    }
    public String toString() {
        return "Sun EC private key, " + params.getCurve().getField().getFieldSize()
            + " bits\n  private value:  "
            + s + "\n  parameters: " + params;
    }
}
