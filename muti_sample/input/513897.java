public class Support_TestProvider extends Provider {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "TestProvider";
    private static final double VERSION = 1.0;
    private static final String INFO = NAME
            + " DSA key, parameter generation and signing; SHA-1 digest; "
            + "SHA1PRNG SecureRandom; PKCS#12/Netscape KeyStore";
    public Support_TestProvider() {
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
                put("Alg.Alias.MessageDigest.SHA1", "SHA");
                put("Alg.Alias.MessageDigest.SHA-1", "SHA");
                put("Alg.Alias.MessageDigest.OID.1.3.14.3.2.26", "SHA");
                put("Alg.Alias.MessageDigest.1.3.14.3.2.26", "SHA");
                put("AlgorithmParameterGenerator.DSA",
                        "made.up.provider.name.AlgorithmParameterGeneratorDSA");
                put("AlgorithmParameters.DSA",
                        "made.up.provider.name.AlgorithmParametersDSA");
                put("Alg.Alias.AlgorithmParameters.1.2.840.10040.4.1", "DSA");
                put("Alg.Alias.AlgorithmParameters.1.3.14.3.2.12", "DSA");
                put("KeyPairGenerator.DSA",
                        "made.up.provider.name.KeyPairGeneratorDSA");
                put("Alg.Alias.KeyPairGenerator.OID.1.2.840.10040.4.1", "DSA");
                put("Alg.Alias.KeyPairGenerator.1.2.840.10040.4.1", "DSA");
                put("Alg.Alias.KeyPairGenerator.1.3.14.3.2.12", "DSA");
                put("KeyFactory.DSA", "made.up.provider.name.KeyFactoryDSA");
                put("KeyFactory.RSA", "made.up.provider.name.KeyFactoryRSA");
                put("Alg.Alias.KeyFactory.1.2.840.10040.4.1", "DSA");
                put("Alg.Alias.KeyFactory.1.3.14.3.2.12", "DSA");
                put("Signature.SHA1withDSA",
                        "made.up.provider.name.SignatureDSA");
                put("Alg.Alias.Signature.DSA", "SHA1withDSA");
                put("Alg.Alias.Signature.DSS", "SHA1withDSA");
                put("Alg.Alias.Signature.SHA/DSA", "SHA1withDSA");
                put("Alg.Alias.Signature.SHA1/DSA", "SHA1withDSA");
                put("Alg.Alias.Signature.SHA-1/DSA", "SHA1withDSA");
                put("Alg.Alias.Signature.SHAwithDSA", "SHA1withDSA");
                put("Alg.Alias.Signature.DSAwithSHA1", "SHA1withDSA");
                put("Alg.Alias.Signature.DSAWithSHA1", "SHA1withDSA");
                put("Alg.Alias.Signature.SHA-1withDSA", "SHA1withDSA");
                put("Alg.Alias.Signature.OID.1.2.840.10040.4.3", "SHA1withDSA");
                put("Alg.Alias.Signature.1.2.840.10040.4.3", "SHA1withDSA");
                put("Alg.Alias.Signature.1.3.14.3.2.13", "SHA1withDSA");
                put("Alg.Alias.Signature.1.3.14.3.2.27", "SHA1withDSA");
                put("Alg.Alias.Signature.OID.1.3.14.3.2.13", "SHA1withDSA");
                put("Alg.Alias.Signature.OID.1.3.14.3.2.27", "SHA1withDSA");
                put("KeyStore.PKCS#12/Netscape",
                        "tests.support.Support_DummyPKCS12Keystore");
                put("CertificateFactory.X509",
                        "made.up.provider.name.CertificateFactoryX509");
                put("Alg.Alias.CertificateFactory.X.509", "X509");
                return null;
            }
        });
    }
}