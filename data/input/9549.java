class MyX509ExtendedKeyManager extends X509ExtendedKeyManager {
    X509ExtendedKeyManager akm;
    MyX509ExtendedKeyManager(X509ExtendedKeyManager akm) {
        this.akm = akm;
    }
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        System.out.println("Calling from X509ExtendedKeyManager");
        return akm.getClientAliases(keyType, issuers);
    }
    public String chooseClientAlias(String[] keyType, Principal[] issuers,
            Socket socket) {
        System.out.println("Calling from X509ExtendedKeyManager");
        return akm.chooseClientAlias(keyType, issuers, socket);
    }
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        System.out.println("Calling from X509ExtendedKeyManager");
        return akm.getServerAliases(keyType, issuers);
    }
    public String chooseServerAlias(String keyType, Principal[] issuers,
            Socket socket) {
        System.out.println("Calling from X509ExtendedKeyManager");
        return akm.chooseServerAlias(keyType, issuers, socket);
    }
    public X509Certificate[] getCertificateChain(String alias) {
        System.out.println("Calling from X509ExtendedKeyManager");
        return akm.getCertificateChain(alias);
    }
    public PrivateKey getPrivateKey(String alias) {
        System.out.println("Calling from X509ExtendedKeyManager");
        return akm.getPrivateKey(alias);
    }
    public String chooseEngineClientAlias(String[] keyType,
            Principal[] issuers, SSLEngine engine) {
        System.out.println("Calling from X509ExtendedKeyManager");
        return akm.chooseEngineClientAlias(keyType, issuers, engine);
    }
    public String chooseEngineServerAlias(String keyType,
            Principal[] issuers, SSLEngine engine) {
        System.out.println("Calling from X509ExtendedKeyManager");
        return akm.chooseEngineServerAlias(keyType, issuers, engine);
    }
}
