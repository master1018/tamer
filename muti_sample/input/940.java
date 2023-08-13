public class TimestampToken {
    private int version;
    private ObjectIdentifier policy;
    private BigInteger serialNumber;
    private AlgorithmId hashAlgorithm;
    private byte[] hashedMessage;
    private Date genTime;
    private BigInteger nonce;
    public TimestampToken(byte[] timestampTokenInfo) throws IOException {
        if (timestampTokenInfo == null) {
            throw new IOException("No timestamp token info");
        }
        parse(timestampTokenInfo);
    }
    public Date getDate() {
        return genTime;
    }
    public AlgorithmId getHashAlgorithm() {
        return hashAlgorithm;
    }
    public byte[] getHashedMessage() {
        return hashedMessage;
    }
    public BigInteger getNonce() {
        return nonce;
    }
    private void parse(byte[] timestampTokenInfo) throws IOException {
        DerValue tstInfo = new DerValue(timestampTokenInfo);
        if (tstInfo.tag != DerValue.tag_Sequence) {
            throw new IOException("Bad encoding for timestamp token info");
        }
        version = tstInfo.data.getInteger();
        policy = tstInfo.data.getOID();
        DerValue messageImprint = tstInfo.data.getDerValue();
        hashAlgorithm = AlgorithmId.parse(messageImprint.data.getDerValue());
        hashedMessage = messageImprint.data.getOctetString();
        serialNumber = tstInfo.data.getBigInteger();
        genTime = tstInfo.data.getGeneralizedTime();
        while (tstInfo.data.available() > 0) {
            DerValue d = tstInfo.data.getDerValue();
            if (d.tag == DerValue.tag_Integer) {    
                nonce = d.getBigInteger();
                break;
            }
        }
    }
}
