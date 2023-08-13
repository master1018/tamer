final class PrivateKeyInfo {
    private static final BigInteger VERSION = BigInteger.ZERO;
    private AlgorithmId algid;
    private byte[] privkey;
    PrivateKeyInfo(byte[] encoded) throws IOException {
        DerValue val = new DerValue(encoded);
        if (val.tag != DerValue.tag_Sequence)
            throw new IOException("private key parse error: not a sequence");
        BigInteger parsedVersion = val.data.getBigInteger();
        if (!parsedVersion.equals(VERSION)) {
            throw new IOException("version mismatch: (supported: " +
                                  VERSION + ", parsed: " + parsedVersion);
        }
        this.algid = AlgorithmId.parse(val.data.getDerValue());
        this.privkey = val.data.getOctetString();
    }
    AlgorithmId getAlgorithm() {
        return this.algid;
    }
}
