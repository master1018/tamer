public final class JSSEProvider extends Provider {
    private static final long serialVersionUID = 3075686092260669675L;
    public JSSEProvider() {
        super("HarmonyJSSE", 1.0, "Harmony JSSE Provider");
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                put("SSLContext.TLS", SSLContextImpl.class.getName());
                put("Alg.Alias.SSLContext.TLSv1", "TLS");
                put("KeyManagerFactory.X509", KeyManagerFactoryImpl.class.getName());
                put("TrustManagerFactory.X509", TrustManagerFactoryImpl.class.getName());
                put("MessageDigest.SHA-1", "org.apache.harmony.xnet.provider.jsse.OpenSSLMessageDigestJDK$SHA1");
                put("Alg.Alias.MessageDigest.SHA1", "SHA-1");
                put("Alg.Alias.MessageDigest.SHA", "SHA-1");
                put("Alg.Alias.MessageDigest.1.3.14.3.2.26", "SHA-1");
                put("MessageDigest.SHA-224", "org.apache.harmony.xnet.provider.jsse.OpenSSLMessageDigestJDK$SHA224");
                put("Alg.Alias.MessageDigest.SHA224", "SHA-224");
                put("Alg.Alias.MessageDigest.2.16.840.1.101.3.4.2.4", "SHA-224");
                put("MessageDigest.SHA-256", "org.apache.harmony.xnet.provider.jsse.OpenSSLMessageDigestJDK$SHA256");
                put("Alg.Alias.MessageDigest.SHA256", "SHA-256");
                put("Alg.Alias.MessageDigest.2.16.840.1.101.3.4.2.1", "SHA-256");
                put("MessageDigest.MD5", "org.apache.harmony.xnet.provider.jsse.OpenSSLMessageDigestJDK$MD5");
                put("Alg.Alias.MessageDigest.1.2.840.113549.2.5", "MD5");
                return null;
            }
        });
    }
}
