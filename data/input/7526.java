public class NullCases {
    public static void main(String[] args) throws Exception {
        KeyManagerFactory kmf;
        X509KeyManager km;
        char [] password = {' '};
        kmf = KeyManagerFactory.getInstance("NewSunX509");
        kmf.init((KeyStore)null, password);
        km = (X509KeyManager) kmf.getKeyManagers()[0];
        X509Certificate[] certs = km.getCertificateChain("doesnotexist");
        PrivateKey priv = km.getPrivateKey("doesnotexist");
        if (certs != null || priv != null) {
            throw new Exception("Should return null if the alias can't be found");
        }
        String[] clis = km.getClientAliases("doesnotexist", null);
        if (clis != null && clis.length == 0) {
            throw new Exception("Should return null instead of empty array");
        }
        String[] srvs = km.getServerAliases("doesnotexist", null);
        if (srvs != null && srvs.length == 0) {
            throw new Exception("Should return null instead of empty array");
        }
        km.getServerAliases(null, null);
        km.getClientAliases(null, null);
        km.getCertificateChain(null);
        km.getPrivateKey(null);
        km.chooseServerAlias(null, null, null);
        km.chooseClientAlias(null, null, null);
    }
}
