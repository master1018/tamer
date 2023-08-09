public final class TlsMasterSecretGenerator extends KeyGeneratorSpi {
    private final static String MSG = "TlsMasterSecretGenerator must be "
        + "initialized using a TlsMasterSecretParameterSpec";
    private TlsMasterSecretParameterSpec spec;
    private int protocolVersion;
    public TlsMasterSecretGenerator() {
    }
    protected void engineInit(SecureRandom random) {
        throw new InvalidParameterException(MSG);
    }
    protected void engineInit(AlgorithmParameterSpec params,
            SecureRandom random) throws InvalidAlgorithmParameterException {
        if (params instanceof TlsMasterSecretParameterSpec == false) {
            throw new InvalidAlgorithmParameterException(MSG);
        }
        this.spec = (TlsMasterSecretParameterSpec)params;
        if ("RAW".equals(spec.getPremasterSecret().getFormat()) == false) {
            throw new InvalidAlgorithmParameterException(
                "Key format must be RAW");
        }
        protocolVersion = (spec.getMajorVersion() << 8)
            | spec.getMinorVersion();
        if ((protocolVersion < 0x0300) || (protocolVersion > 0x0303)) {
            throw new InvalidAlgorithmParameterException(
                "Only SSL 3.0, TLS 1.0/1.1/1.2 supported");
        }
    }
    protected void engineInit(int keysize, SecureRandom random) {
        throw new InvalidParameterException(MSG);
    }
    protected SecretKey engineGenerateKey() {
        if (spec == null) {
            throw new IllegalStateException(
                "TlsMasterSecretGenerator must be initialized");
        }
        SecretKey premasterKey = spec.getPremasterSecret();
        byte[] premaster = premasterKey.getEncoded();
        int premasterMajor, premasterMinor;
        if (premasterKey.getAlgorithm().equals("TlsRsaPremasterSecret")) {
            premasterMajor = premaster[0] & 0xff;
            premasterMinor = premaster[1] & 0xff;
        } else {
            premasterMajor = -1;
            premasterMinor = -1;
        }
        try {
            byte[] master;
            byte[] clientRandom = spec.getClientRandom();
            byte[] serverRandom = spec.getServerRandom();
            if (protocolVersion >= 0x0301) {
                byte[] seed = concat(clientRandom, serverRandom);
                master = ((protocolVersion >= 0x0303) ?
                    doTLS12PRF(premaster, LABEL_MASTER_SECRET, seed, 48,
                        spec.getPRFHashAlg(), spec.getPRFHashLength(),
                        spec.getPRFBlockSize()) :
                    doTLS10PRF(premaster, LABEL_MASTER_SECRET, seed, 48));
            } else {
                master = new byte[48];
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                MessageDigest sha = MessageDigest.getInstance("SHA");
                byte[] tmp = new byte[20];
                for (int i = 0; i < 3; i++) {
                    sha.update(SSL3_CONST[i]);
                    sha.update(premaster);
                    sha.update(clientRandom);
                    sha.update(serverRandom);
                    sha.digest(tmp, 0, 20);
                    md5.update(premaster);
                    md5.update(tmp);
                    md5.digest(master, i << 4, 16);
                }
            }
            return new TlsMasterSecretKey(master, premasterMajor,
                premasterMinor);
        } catch (NoSuchAlgorithmException e) {
            throw new ProviderException(e);
        } catch (DigestException e) {
            throw new ProviderException(e);
        }
    }
    private static final class TlsMasterSecretKey implements TlsMasterSecret {
        private byte[] key;
        private final int majorVersion, minorVersion;
        TlsMasterSecretKey(byte[] key, int majorVersion, int minorVersion) {
            this.key = key;
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
        }
        public int getMajorVersion() {
            return majorVersion;
        }
        public int getMinorVersion() {
            return minorVersion;
        }
        public String getAlgorithm() {
            return "TlsMasterSecret";
        }
        public String getFormat() {
            return "RAW";
        }
        public byte[] getEncoded() {
            return key.clone();
        }
    }
}
