public final class SunMSCAPI extends Provider {
    private static final long serialVersionUID = 8622598936488630849L; 
    private static final String INFO = "Sun's Microsoft Crypto API provider";
    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                System.loadLibrary("sunmscapi");
                return null;
            }
        });
    }
    public SunMSCAPI() {
        super("SunMSCAPI", 1.7d, INFO);
        final Map map = (System.getSecurityManager() == null)
                        ? (Map)this : new HashMap();
        map.put("SecureRandom.Windows-PRNG", "sun.security.mscapi.PRNG");
        map.put("KeyStore.Windows-MY", "sun.security.mscapi.KeyStore$MY");
        map.put("KeyStore.Windows-ROOT", "sun.security.mscapi.KeyStore$ROOT");
        map.put("Signature.NONEwithRSA",
            "sun.security.mscapi.RSASignature$Raw");
        map.put("Signature.SHA1withRSA",
            "sun.security.mscapi.RSASignature$SHA1");
        map.put("Signature.SHA256withRSA",
            "sun.security.mscapi.RSASignature$SHA256");
        map.put("Signature.SHA384withRSA",
            "sun.security.mscapi.RSASignature$SHA384");
        map.put("Signature.SHA512withRSA",
            "sun.security.mscapi.RSASignature$SHA512");
        map.put("Signature.MD5withRSA",
            "sun.security.mscapi.RSASignature$MD5");
        map.put("Signature.MD2withRSA",
            "sun.security.mscapi.RSASignature$MD2");
        map.put("Signature.NONEwithRSA SupportedKeyClasses",
            "sun.security.mscapi.Key");
        map.put("Signature.SHA1withRSA SupportedKeyClasses",
            "sun.security.mscapi.Key");
        map.put("Signature.SHA256withRSA SupportedKeyClasses",
            "sun.security.mscapi.Key");
        map.put("Signature.SHA384withRSA SupportedKeyClasses",
            "sun.security.mscapi.Key");
        map.put("Signature.SHA512withRSA SupportedKeyClasses",
            "sun.security.mscapi.Key");
        map.put("Signature.MD5withRSA SupportedKeyClasses",
            "sun.security.mscapi.Key");
        map.put("Signature.MD2withRSA SupportedKeyClasses",
            "sun.security.mscapi.Key");
        map.put("KeyPairGenerator.RSA",
            "sun.security.mscapi.RSAKeyPairGenerator");
        map.put("KeyPairGenerator.RSA KeySize", "1024");
        map.put("Cipher.RSA", "sun.security.mscapi.RSACipher");
        map.put("Cipher.RSA/ECB/PKCS1Padding",
            "sun.security.mscapi.RSACipher");
        map.put("Cipher.RSA SupportedModes", "ECB");
        map.put("Cipher.RSA SupportedPaddings", "PKCS1PADDING");
        map.put("Cipher.RSA SupportedKeyClasses", "sun.security.mscapi.Key");
        if (map != this) {
            AccessController.doPrivileged(new PutAllAction(this, map));
        }
    }
}
