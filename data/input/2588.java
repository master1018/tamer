public final class Provider extends java.security.Provider {
    private static final long serialVersionUID = 8622598936488630849L;
    private static final String info = "Sun SASL provider" +
        "(implements client mechanisms for: " +
        "DIGEST-MD5, GSSAPI, EXTERNAL, PLAIN, CRAM-MD5, NTLM;" +
        " server mechanisms for: DIGEST-MD5, GSSAPI, CRAM-MD5, NTLM)";
    public Provider() {
        super("SunSASL", 1.7d, info);
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                put("SaslClientFactory.DIGEST-MD5",
                    "com.sun.security.sasl.digest.FactoryImpl");
                put("SaslClientFactory.NTLM",
                    "com.sun.security.sasl.ntlm.FactoryImpl");
                put("SaslClientFactory.GSSAPI",
                    "com.sun.security.sasl.gsskerb.FactoryImpl");
                put("SaslClientFactory.EXTERNAL",
                    "com.sun.security.sasl.ClientFactoryImpl");
                put("SaslClientFactory.PLAIN",
                    "com.sun.security.sasl.ClientFactoryImpl");
                put("SaslClientFactory.CRAM-MD5",
                    "com.sun.security.sasl.ClientFactoryImpl");
                put("SaslServerFactory.CRAM-MD5",
                    "com.sun.security.sasl.ServerFactoryImpl");
                put("SaslServerFactory.GSSAPI",
                    "com.sun.security.sasl.gsskerb.FactoryImpl");
                put("SaslServerFactory.DIGEST-MD5",
                    "com.sun.security.sasl.digest.FactoryImpl");
                put("SaslServerFactory.NTLM",
                    "com.sun.security.sasl.ntlm.FactoryImpl");
                return null;
            }
        });
    }
}
