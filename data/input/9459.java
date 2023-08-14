class MyX509KeyManager implements X509KeyManager  {
    X509KeyManager km;
    MyX509KeyManager(X509KeyManager km) {
        this.km = km;
    }
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        System.out.println("Calling from X509KeyManager");
        return km.getClientAliases(keyType, issuers);
    }
    public String chooseClientAlias(String[] keyType, Principal[] issuers,
            Socket socket) {
        System.out.println("Calling from X509KeyManager");
        return km.chooseClientAlias(keyType, issuers, socket);
    }
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        System.out.println("Calling from X509KeyManager");
        return km.getServerAliases(keyType, issuers);
    }
    public String chooseServerAlias(String keyType, Principal[] issuers,
            Socket socket) {
        System.out.println("Calling from X509KeyManager");
        return km.chooseServerAlias(keyType, issuers, socket);
    }
    public X509Certificate[] getCertificateChain(String alias) {
        System.out.println("Calling from X509KeyManager");
        return km.getCertificateChain(alias);
    }
    public PrivateKey getPrivateKey(String alias) {
        System.out.println("Calling from X509KeyManager");
        return km.getPrivateKey(alias);
    }
}
