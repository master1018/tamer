final class EncryptedPrivateKeyInfo {
    private AlgorithmId algid;
    private byte[] encryptedData;
    private byte[] encoded;
    EncryptedPrivateKeyInfo(byte[] encoded) throws IOException {
        DerValue val = new DerValue(encoded);
        DerValue[] seq = new DerValue[2];
        seq[0] = val.data.getDerValue();
        seq[1] = val.data.getDerValue();
        if (val.data.available() != 0) {
            throw new IOException("overrun, bytes = " + val.data.available());
        }
        this.algid = AlgorithmId.parse(seq[0]);
        if (seq[0].data.available() != 0) {
            throw new IOException("encryptionAlgorithm field overrun");
        }
        this.encryptedData = seq[1].getOctetString();
        if (seq[1].data.available() != 0)
            throw new IOException("encryptedData field overrun");
        this.encoded = (byte[])encoded.clone();
    }
    EncryptedPrivateKeyInfo(AlgorithmId algid, byte[] encryptedData) {
        this.algid = algid;
        this.encryptedData = (byte[])encryptedData.clone();
        this.encoded = null; 
    }
    AlgorithmId getAlgorithm() {
        return this.algid;
    }
    byte[] getEncryptedData() {
        return (byte[])this.encryptedData.clone();
    }
    byte[] getEncoded()
        throws IOException
    {
        if (this.encoded != null) return (byte[])this.encoded.clone();
        DerOutputStream out = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();
        algid.encode(tmp);
        tmp.putOctetString(encryptedData);
        out.write(DerValue.tag_Sequence, tmp);
        this.encoded = out.toByteArray();
        return (byte[])this.encoded.clone();
    }
}
