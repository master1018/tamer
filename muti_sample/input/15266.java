public class DummyProvider extends Provider {
    public DummyProvider() {
        super("Dummy", 0.1, "Dummy Provider");
        put("KeyStore.DKS", "sun.security.provider.JavaKeyStore$JKS");
        put("Signature.SHA1withDSA",
            "sun.security.provider.DSA$SHA1withDSA");
        put("Alg.Alias.Signature.DSA", "SHA1withDSA");
        put("KeyPairGenerator.DSA",
            "sun.security.provider.DSAKeyPairGenerator");
        put("MessageDigest.SHA", "sun.security.provider.SHA");
        put("Alg.Alias.MessageDigest.SHA1", "SHA");
        put("AlgorithmParameterGenerator.DSA",
            "sun.security.provider.DSAParameterGenerator");
        put("AlgorithmParameters.DSA",
            "sun.security.provider.DSAParameters");
        put("KeyFactory.DSA", "sun.security.provider.DSAKeyFactory");
        put("CertificateFactory.X.509",
            "sun.security.provider.X509Factory");
        put("Alg.Alias.CertificateFactory.X509", "X.509");
    }
}
