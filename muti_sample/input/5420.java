class MacData {
    private String digestAlgorithmName;
    private AlgorithmParameters digestAlgorithmParams;
    private byte[] digest;
    private byte[] macSalt;
    private int iterations;
    private byte[] encoded = null;
    MacData(DerInputStream derin)
        throws IOException, ParsingException
    {
        DerValue[] macData = derin.getSequence(2);
        DerInputStream digestIn = new DerInputStream(macData[0].toByteArray());
        DerValue[] digestInfo = digestIn.getSequence(2);
        AlgorithmId digestAlgorithmId = AlgorithmId.parse(digestInfo[0]);
        this.digestAlgorithmName = digestAlgorithmId.getName();
        this.digestAlgorithmParams = digestAlgorithmId.getParameters();
        this.digest = digestInfo[1].getOctetString();
        this.macSalt = macData[1].getOctetString();
        if (macData.length > 2) {
            this.iterations = macData[2].getInteger();
        } else {
            this.iterations = 1;
        }
    }
    MacData(String algName, byte[] digest, byte[] salt, int iterations)
        throws NoSuchAlgorithmException
    {
        if (algName == null)
           throw new NullPointerException("the algName parameter " +
                                               "must be non-null");
        AlgorithmId algid = AlgorithmId.get(algName);
        this.digestAlgorithmName = algid.getName();
        this.digestAlgorithmParams = algid.getParameters();
        if (digest == null) {
            throw new NullPointerException("the digest " +
                                           "parameter must be non-null");
        } else if (digest.length == 0) {
            throw new IllegalArgumentException("the digest " +
                                                "parameter must not be empty");
        } else {
            this.digest = digest.clone();
        }
        this.macSalt = salt;
        this.iterations = iterations;
        this.encoded = null;
    }
    MacData(AlgorithmParameters algParams, byte[] digest,
        byte[] salt, int iterations) throws NoSuchAlgorithmException
    {
        if (algParams == null)
           throw new NullPointerException("the algParams parameter " +
                                               "must be non-null");
        AlgorithmId algid = AlgorithmId.get(algParams);
        this.digestAlgorithmName = algid.getName();
        this.digestAlgorithmParams = algid.getParameters();
        if (digest == null) {
            throw new NullPointerException("the digest " +
                                           "parameter must be non-null");
        } else if (digest.length == 0) {
            throw new IllegalArgumentException("the digest " +
                                                "parameter must not be empty");
        } else {
            this.digest = digest.clone();
        }
        this.macSalt = salt;
        this.iterations = iterations;
        this.encoded = null;
    }
    String getDigestAlgName() {
        return digestAlgorithmName;
    }
    byte[] getSalt() {
        return macSalt;
    }
    int getIterations() {
        return iterations;
    }
    byte[] getDigest() {
        return digest;
    }
    public byte[] getEncoded() throws NoSuchAlgorithmException, IOException
    {
        if (this.encoded != null)
            return this.encoded.clone();
        DerOutputStream out = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();
        DerOutputStream tmp2 = new DerOutputStream();
        AlgorithmId algid = AlgorithmId.get(digestAlgorithmName);
        algid.encode(tmp2);
        tmp2.putOctetString(digest);
        tmp.write(DerValue.tag_Sequence, tmp2);
        tmp.putOctetString(macSalt);
        tmp.putInteger(iterations);
        out.write(DerValue.tag_Sequence, tmp);
        this.encoded = out.toByteArray();
        return this.encoded.clone();
    }
}
