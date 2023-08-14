final class P11TlsPrfGenerator extends KeyGeneratorSpi {
    private final static String MSG =
            "TlsPrfGenerator must be initialized using a TlsPrfParameterSpec";
    private final Token token;
    private final String algorithm;
    private final long mechanism;
    private TlsPrfParameterSpec spec;
    private P11Key p11Key;
    P11TlsPrfGenerator(Token token, String algorithm, long mechanism)
            throws PKCS11Exception {
        super();
        this.token = token;
        this.algorithm = algorithm;
        this.mechanism = mechanism;
    }
    protected void engineInit(SecureRandom random) {
        throw new InvalidParameterException(MSG);
    }
    protected void engineInit(AlgorithmParameterSpec params,
            SecureRandom random) throws InvalidAlgorithmParameterException {
        if (params instanceof TlsPrfParameterSpec == false) {
            throw new InvalidAlgorithmParameterException(MSG);
        }
        this.spec = (TlsPrfParameterSpec)params;
        SecretKey key = spec.getSecret();
        if (key == null) {
            key = NULL_KEY;
        }
        try {
            p11Key = P11SecretKeyFactory.convertKey(token, key, null);
        } catch (InvalidKeyException e) {
            throw new InvalidAlgorithmParameterException("init() failed", e);
        }
    }
    private static final SecretKey NULL_KEY = new SecretKey() {
        public byte[] getEncoded() {
            return new byte[0];
        }
        public String getFormat() {
            return "RAW";
        }
        public String getAlgorithm() {
            return "Generic";
        }
    };
    protected void engineInit(int keysize, SecureRandom random) {
        throw new InvalidParameterException(MSG);
    }
    protected SecretKey engineGenerateKey() {
        if (spec == null) {
            throw new IllegalStateException("TlsPrfGenerator must be initialized");
        }
        byte[] label = P11Util.getBytesUTF8(spec.getLabel());
        byte[] seed = spec.getSeed();
        if (mechanism == CKM_NSS_TLS_PRF_GENERAL) {
            Session session = null;
            try {
                session = token.getOpSession();
                token.p11.C_SignInit
                    (session.id(), new CK_MECHANISM(mechanism), p11Key.keyID);
                token.p11.C_SignUpdate(session.id(), 0, label, 0, label.length);
                token.p11.C_SignUpdate(session.id(), 0, seed, 0, seed.length);
                byte[] out = token.p11.C_SignFinal
                                    (session.id(), spec.getOutputLength());
                return new SecretKeySpec(out, "TlsPrf");
            } catch (PKCS11Exception e) {
                throw new ProviderException("Could not calculate PRF", e);
            } finally {
                token.releaseSession(session);
            }
        }
        byte[] out = new byte[spec.getOutputLength()];
        CK_TLS_PRF_PARAMS params = new CK_TLS_PRF_PARAMS(seed, label, out);
        Session session = null;
        try {
            session = token.getOpSession();
            long keyID = token.p11.C_DeriveKey(session.id(),
                new CK_MECHANISM(mechanism, params), p11Key.keyID, null);
            return new SecretKeySpec(out, "TlsPrf");
        } catch (PKCS11Exception e) {
            throw new ProviderException("Could not calculate PRF", e);
        } finally {
            token.releaseSession(session);
        }
    }
}
