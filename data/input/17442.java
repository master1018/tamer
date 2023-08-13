public class CertId {
    private static final boolean debug = false;
    private static final AlgorithmId SHA1_ALGID
        = new AlgorithmId(AlgorithmId.SHA_oid);
    private final AlgorithmId hashAlgId;
    private final byte[] issuerNameHash;
    private final byte[] issuerKeyHash;
    private final SerialNumber certSerialNumber;
    private int myhash = -1; 
    public CertId(X509Certificate issuerCert, SerialNumber serialNumber)
        throws IOException {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException nsae) {
            throw new IOException("Unable to create CertId", nsae);
        }
        hashAlgId = SHA1_ALGID;
        md.update(issuerCert.getSubjectX500Principal().getEncoded());
        issuerNameHash = md.digest();
        byte[] pubKey = issuerCert.getPublicKey().getEncoded();
        DerValue val = new DerValue(pubKey);
        DerValue[] seq = new DerValue[2];
        seq[0] = val.data.getDerValue(); 
        seq[1] = val.data.getDerValue(); 
        byte[] keyBytes = seq[1].getBitString();
        md.update(keyBytes);
        issuerKeyHash = md.digest();
        certSerialNumber = serialNumber;
        if (debug) {
            HexDumpEncoder encoder = new HexDumpEncoder();
            System.out.println("Issuer Certificate is " + issuerCert);
            System.out.println("issuerNameHash is " +
                encoder.encodeBuffer(issuerNameHash));
            System.out.println("issuerKeyHash is " +
                encoder.encodeBuffer(issuerKeyHash));
            System.out.println("SerialNumber is " + serialNumber.getNumber());
        }
    }
    public CertId(DerInputStream derIn) throws IOException {
        hashAlgId = AlgorithmId.parse(derIn.getDerValue());
        issuerNameHash = derIn.getOctetString();
        issuerKeyHash = derIn.getOctetString();
        certSerialNumber = new SerialNumber(derIn);
    }
    public AlgorithmId getHashAlgorithm() {
        return hashAlgId;
    }
    public byte[] getIssuerNameHash() {
        return issuerNameHash;
    }
    public byte[] getIssuerKeyHash() {
        return issuerKeyHash;
    }
    public BigInteger getSerialNumber() {
        return certSerialNumber.getNumber();
    }
    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        hashAlgId.encode(tmp);
        tmp.putOctetString(issuerNameHash);
        tmp.putOctetString(issuerKeyHash);
        certSerialNumber.encode(tmp);
        out.write(DerValue.tag_Sequence, tmp);
        if (debug) {
            HexDumpEncoder encoder = new HexDumpEncoder();
            System.out.println("Encoded certId is " +
                encoder.encode(out.toByteArray()));
        }
    }
    @Override public int hashCode() {
        if (myhash == -1) {
            myhash = hashAlgId.hashCode();
            for (int i = 0; i < issuerNameHash.length; i++) {
                myhash += issuerNameHash[i] * i;
            }
            for (int i = 0; i < issuerKeyHash.length; i++) {
                myhash += issuerKeyHash[i] * i;
            }
            myhash += certSerialNumber.getNumber().hashCode();
        }
        return myhash;
    }
    @Override public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || (!(other instanceof CertId))) {
            return false;
        }
        CertId that = (CertId) other;
        if (hashAlgId.equals(that.getHashAlgorithm()) &&
            Arrays.equals(issuerNameHash, that.getIssuerNameHash()) &&
            Arrays.equals(issuerKeyHash, that.getIssuerKeyHash()) &&
            certSerialNumber.getNumber().equals(that.getSerialNumber())) {
            return true;
        } else {
            return false;
        }
    }
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CertId \n");
        sb.append("Algorithm: " + hashAlgId.toString() +"\n");
        sb.append("issuerNameHash \n");
        HexDumpEncoder encoder = new HexDumpEncoder();
        sb.append(encoder.encode(issuerNameHash));
        sb.append("\nissuerKeyHash: \n");
        sb.append(encoder.encode(issuerKeyHash));
        sb.append("\n" +  certSerialNumber.toString());
        return sb.toString();
    }
}
