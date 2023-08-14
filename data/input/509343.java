public class Support_ProviderTrust extends Provider {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "ProviderTrust";
    private static final double VERSION = 1.0;
    private static final String INFO = NAME
            + " DSA key, parameter generation and signing; SHA-1 digest; SHA1PRNG SecureRandom";
    public Support_ProviderTrust() {
        super(NAME, VERSION, INFO);
        registerServices();
    }
    private void registerServices() {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                put("MessageDigest.SHA",
                        "made.up.provider.name.MessageDigestSHA");
                put("MessageDigest.MD5",
                        "made.up.provider.name.MessageDigestMD5");
                put("AlgorithmParameterGenerator.DSA",
                        "made.up.provider.name.AlgorithmParameterGeneratorDSA");
                put("AlgorithmParameters.DSA",
                        "made.up.provider.name.AlgorithmParametersDSA");
                put("KeyPairGenerator.DSA",
                        "made.up.provider.name.KeyPairGeneratorDSA");
                put("KeyFactory.DSA", "made.up.provider.name.KeyFactoryDSA");
                put("KeyFactory.RSA", "made.up.provider.name.KeyFactoryRSA");
                put("Signature.SHA1withDSA",
                        "made.up.provider.name.SignatureDSA");
                put("KeyStore.PKCS#12/Netscape",
                        "made.up.provider.name.KeyStore");
                put("CertificateFactory.X509",
                        "made.up.provider.name.CertificateFactoryX509");
                return null;
            }
        });
    }
}