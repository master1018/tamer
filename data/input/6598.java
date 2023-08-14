final class SunEntries {
    private SunEntries() {
    }
    static void putEntries(Map<Object, Object> map) {
        boolean nativeAvailable = NativePRNG.isAvailable();
        boolean useUrandom = seedSource.equals(URL_DEV_URANDOM);
        if (nativeAvailable && useUrandom) {
            map.put("SecureRandom.NativePRNG",
                "sun.security.provider.NativePRNG");
        }
        map.put("SecureRandom.SHA1PRNG",
             "sun.security.provider.SecureRandom");
        if (nativeAvailable && !useUrandom) {
            map.put("SecureRandom.NativePRNG",
                "sun.security.provider.NativePRNG");
        }
        map.put("Signature.SHA1withDSA", "sun.security.provider.DSA$SHA1withDSA");
        map.put("Signature.NONEwithDSA", "sun.security.provider.DSA$RawDSA");
        map.put("Alg.Alias.Signature.RawDSA", "NONEwithDSA");
        String dsaKeyClasses = "java.security.interfaces.DSAPublicKey" +
                "|java.security.interfaces.DSAPrivateKey";
        map.put("Signature.SHA1withDSA SupportedKeyClasses", dsaKeyClasses);
        map.put("Signature.NONEwithDSA SupportedKeyClasses", dsaKeyClasses);
        map.put("Alg.Alias.Signature.DSA", "SHA1withDSA");
        map.put("Alg.Alias.Signature.DSS", "SHA1withDSA");
        map.put("Alg.Alias.Signature.SHA/DSA", "SHA1withDSA");
        map.put("Alg.Alias.Signature.SHA-1/DSA", "SHA1withDSA");
        map.put("Alg.Alias.Signature.SHA1/DSA", "SHA1withDSA");
        map.put("Alg.Alias.Signature.SHAwithDSA", "SHA1withDSA");
        map.put("Alg.Alias.Signature.DSAWithSHA1", "SHA1withDSA");
        map.put("Alg.Alias.Signature.OID.1.2.840.10040.4.3",
            "SHA1withDSA");
        map.put("Alg.Alias.Signature.1.2.840.10040.4.3", "SHA1withDSA");
        map.put("Alg.Alias.Signature.1.3.14.3.2.13", "SHA1withDSA");
        map.put("Alg.Alias.Signature.1.3.14.3.2.27", "SHA1withDSA");
        map.put("KeyPairGenerator.DSA",
            "sun.security.provider.DSAKeyPairGenerator");
        map.put("Alg.Alias.KeyPairGenerator.OID.1.2.840.10040.4.1", "DSA");
        map.put("Alg.Alias.KeyPairGenerator.1.2.840.10040.4.1", "DSA");
        map.put("Alg.Alias.KeyPairGenerator.1.3.14.3.2.12", "DSA");
        map.put("MessageDigest.MD2", "sun.security.provider.MD2");
        map.put("MessageDigest.MD5", "sun.security.provider.MD5");
        map.put("MessageDigest.SHA", "sun.security.provider.SHA");
        map.put("Alg.Alias.MessageDigest.SHA-1", "SHA");
        map.put("Alg.Alias.MessageDigest.SHA1", "SHA");
        map.put("MessageDigest.SHA-256", "sun.security.provider.SHA2");
        map.put("MessageDigest.SHA-384", "sun.security.provider.SHA5$SHA384");
        map.put("MessageDigest.SHA-512", "sun.security.provider.SHA5$SHA512");
        map.put("AlgorithmParameterGenerator.DSA",
            "sun.security.provider.DSAParameterGenerator");
        map.put("AlgorithmParameters.DSA",
            "sun.security.provider.DSAParameters");
        map.put("Alg.Alias.AlgorithmParameters.1.3.14.3.2.12", "DSA");
        map.put("Alg.Alias.AlgorithmParameters.1.2.840.10040.4.1", "DSA");
        map.put("KeyFactory.DSA", "sun.security.provider.DSAKeyFactory");
        map.put("Alg.Alias.KeyFactory.1.3.14.3.2.12", "DSA");
        map.put("Alg.Alias.KeyFactory.1.2.840.10040.4.1", "DSA");
        map.put("CertificateFactory.X.509",
            "sun.security.provider.X509Factory");
        map.put("Alg.Alias.CertificateFactory.X509", "X.509");
        map.put("KeyStore.JKS", "sun.security.provider.JavaKeyStore$JKS");
        map.put("KeyStore.CaseExactJKS",
                        "sun.security.provider.JavaKeyStore$CaseExactJKS");
        map.put("Policy.JavaPolicy", "sun.security.provider.PolicySpiFile");
        map.put("Configuration.JavaLoginConfig",
                        "sun.security.provider.ConfigSpiFile");
        map.put("CertPathBuilder.PKIX",
            "sun.security.provider.certpath.SunCertPathBuilder");
        map.put("CertPathBuilder.PKIX ValidationAlgorithm",
            "RFC3280");
        map.put("CertPathValidator.PKIX",
            "sun.security.provider.certpath.PKIXCertPathValidator");
        map.put("CertPathValidator.PKIX ValidationAlgorithm",
            "RFC3280");
        map.put("CertStore.LDAP",
            "sun.security.provider.certpath.ldap.LDAPCertStore");
        map.put("CertStore.LDAP LDAPSchema", "RFC2587");
        map.put("CertStore.Collection",
            "sun.security.provider.certpath.CollectionCertStore");
        map.put("CertStore.com.sun.security.IndexedCollection",
            "sun.security.provider.certpath.IndexedCollectionCertStore");
        map.put("Signature.SHA1withDSA KeySize", "1024");
        map.put("KeyPairGenerator.DSA KeySize", "1024");
        map.put("AlgorithmParameterGenerator.DSA KeySize", "1024");
        map.put("Signature.SHA1withDSA ImplementedIn", "Software");
        map.put("KeyPairGenerator.DSA ImplementedIn", "Software");
        map.put("MessageDigest.MD5 ImplementedIn", "Software");
        map.put("MessageDigest.SHA ImplementedIn", "Software");
        map.put("AlgorithmParameterGenerator.DSA ImplementedIn",
            "Software");
        map.put("AlgorithmParameters.DSA ImplementedIn", "Software");
        map.put("KeyFactory.DSA ImplementedIn", "Software");
        map.put("SecureRandom.SHA1PRNG ImplementedIn", "Software");
        map.put("CertificateFactory.X.509 ImplementedIn", "Software");
        map.put("KeyStore.JKS ImplementedIn", "Software");
        map.put("CertPathValidator.PKIX ImplementedIn", "Software");
        map.put("CertPathBuilder.PKIX ImplementedIn", "Software");
        map.put("CertStore.LDAP ImplementedIn", "Software");
        map.put("CertStore.Collection ImplementedIn", "Software");
        map.put("CertStore.com.sun.security.IndexedCollection ImplementedIn",
            "Software");
    }
    private final static String PROP_EGD = "java.security.egd";
    private final static String PROP_RNDSOURCE = "securerandom.source";
    final static String URL_DEV_RANDOM = "file:/dev/random";
    final static String URL_DEV_URANDOM = "file:/dev/urandom";
    private static final String seedSource;
    static {
        seedSource = AccessController.doPrivileged(
                new PrivilegedAction<String>() {
            public String run() {
                String egdSource = System.getProperty(PROP_EGD, "");
                if (egdSource.length() != 0) {
                    return egdSource;
                }
                egdSource = Security.getProperty(PROP_RNDSOURCE);
                if (egdSource == null) {
                    return "";
                }
                return egdSource;
            }
        });
    }
    static String getSeedSource() {
        return seedSource;
    }
}
