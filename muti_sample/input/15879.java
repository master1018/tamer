public class KeyIdentifier {
    private byte[] octetString;
    public KeyIdentifier(byte[] octetString) {
        this.octetString = octetString.clone();
    }
    public KeyIdentifier(DerValue val) throws IOException {
        octetString = val.getOctetString();
    }
    public KeyIdentifier(PublicKey pubKey)
        throws IOException
    {
        DerValue algAndKey = new DerValue(pubKey.getEncoded());
        if (algAndKey.tag != DerValue.tag_Sequence)
            throw new IOException("PublicKey value is not a valid "
                                  + "X.509 public key");
        AlgorithmId algid = AlgorithmId.parse(algAndKey.data.getDerValue());
        byte[] key = algAndKey.data.getUnalignedBitString().toByteArray();
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e3) {
            throw new IOException("SHA1 not supported");
        }
        md.update(key);
        this.octetString = md.digest();
    }
    public byte[] getIdentifier() {
        return octetString.clone();
    }
    public String toString() {
        String s = "KeyIdentifier [\n";
        HexDumpEncoder encoder = new HexDumpEncoder();
        s += encoder.encodeBuffer(octetString);
        s += "]\n";
        return (s);
    }
    void encode(DerOutputStream out) throws IOException {
        out.putOctetString(octetString);
    }
    public int hashCode () {
        int retval = 0;
        for (int i = 0; i < octetString.length; i++)
            retval += octetString[i] * i;
        return retval;
    }
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof KeyIdentifier))
            return false;
        return java.util.Arrays.equals(octetString,
                                       ((KeyIdentifier)other).getIdentifier());
    }
}
