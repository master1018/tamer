final class MyKeyManager implements X509KeyManager {
    private HashMap keyMap = new HashMap();
    private HashMap certChainMap = new HashMap();
    MyKeyManager(KeyStore ks, char[] password)
        throws KeyStoreException, NoSuchAlgorithmException,
        UnrecoverableKeyException
    {
        if (ks == null) {
            return;
        }
        Enumeration aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String alias = (String)aliases.nextElement();
            if (ks.isKeyEntry(alias)) {
                Certificate[] certs;
                certs = ks.getCertificateChain(alias);
                if (certs != null && certs.length > 0 &&
                    certs[0] instanceof X509Certificate) {
                    if (!(certs instanceof X509Certificate[])) {
                        Certificate[] tmp = new X509Certificate[certs.length];
                        System.arraycopy(certs, 0, tmp, 0, certs.length);
                        certs = tmp;
                    }
                    Key key = ks.getKey(alias, password);
                    certChainMap.put(alias, certs);
                    keyMap.put(alias, key);
                }
            }
        }
    }
    public String chooseClientAlias(String[] keyTypes, Principal[] issuers,
            Socket socket) {
        return "client";
    }
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        String[] s = new String[1];
        s[0] = "client";
        return s;
    }
    private HashMap serverAliasCache = new HashMap();
    public synchronized String chooseServerAlias(String keyType,
            Principal[] issuers, Socket socket) {
        return "server";
    }
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        String[] s = new String[1];
        s[0] = "server";
        return s;
    }
    public X509Certificate[] getCertificateChain(String alias) {
        Object chain;
        chain = certChainMap.get(alias);
        if (!(chain instanceof X509Certificate[]))
            return null;
        return (X509Certificate[]) chain;
    }
    public PrivateKey getPrivateKey(String alias) {
        Object key;
        key = keyMap.get(alias);
        if (!(key instanceof PrivateKey))
            return null;
        return (PrivateKey)key;
    }
}
