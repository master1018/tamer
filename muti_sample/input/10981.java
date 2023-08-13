public class SelectOneKeyOutOfMany {
    static String pathToStores = "../../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String passwd = "passphrase";
    public static void main(String[] args) throws Exception {
        KeyStore ks;
        KeyManagerFactory kmf;
        X509KeyManager km;
        char[] passphrase = passwd.toCharArray();
        String keyFilename =
            System.getProperty("test.src", ".") + "/" + pathToStores +
                "/" + keyStoreFile;
        kmf = KeyManagerFactory.getInstance("SunX509");
        ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keyFilename), passphrase);
        kmf.init(ks, passphrase);
        km = (X509KeyManager) kmf.getKeyManagers()[0];
        String [] nothing = new String [] { "nothing" };
        String [] rsa = new String [] { "RSA" };
        String [] dsa = new String [] { "DSA" };
        String [] rsaDsa = new String [] { "RSA", "DSA" };
        String [] dsaRsa = new String [] { "DSA", "RSA" };
        String resultsRsaDsa, resultsDsaRsa;
        String resultsRsa, resultsDsa;
        String resultsNone;
        String [] resultArrayRSA;
        String [] resultArrayDSA;
        if (km.getClientAliases("nothing", null) != null)
            throw new Exception("km.getClientAliases(nothing) != null");
        System.out.println("km.getClientAlias(nothing) returning nulls");
        if (km.getServerAliases("nothing", null) != null)
            throw new Exception("km.getServerAliases(nothing) != null");
        System.out.println("km.getServerAlias(nothing) returning nulls");
        System.out.println("=====");
        System.out.println("Dumping Certs...");
        if ((resultArrayRSA = km.getServerAliases("RSA", null)) == null)
            throw new Exception("km.getServerAliases(RSA) == null");
        for (int i = 0; i < resultArrayRSA.length; i++) {
            System.out.println("        resultArrayRSA#" + i + ": " +
                resultArrayRSA[i]);
        }
        if ((resultArrayDSA = km.getServerAliases("DSA", null)) == null)
            throw new Exception("km.getServerAliases(DSA) == null");
        for (int i = 0; i < resultArrayDSA.length; i++) {
            System.out.println("        resultArrayDSA#" + i + ": " +
                resultArrayDSA[i]);
        }
        System.out.println("=====");
        resultsNone = km.chooseClientAlias(nothing, null, null);
        if (resultsNone != null) {
            throw new Exception("km.chooseClientAlias(nothing) != null");
        }
        System.out.println("km.ChooseClientAlias(nothing) passed");
        resultsRsa = km.chooseClientAlias(rsa, null, null);
        if (resultsRsa == null)  {
            throw new Exception(
                "km.chooseClientAlias(rsa, null, null) != null");
        }
        System.out.println("km.chooseClientAlias(rsa) passed");
        resultsDsa = km.chooseClientAlias(dsa, null, null);
        if (resultsDsa == null) {
            throw new Exception(
                "km.chooseClientAlias(dsa, null, null) != null");
        }
        System.out.println("km.chooseClientAlias(dsa) passed");
        resultsRsaDsa = km.chooseClientAlias(rsaDsa, null, null);
        if ((resultsRsaDsa == null) || (resultsRsaDsa != resultsRsa)) {
            throw new Exception("km.chooseClientAlias(rsaDsa) failed");
        }
        System.out.println("km.chooseClientAlias(rsaDsa) passed");
        resultsDsaRsa = km.chooseClientAlias(dsaRsa, null, null);
        if ((resultsDsaRsa == null) || (resultsDsaRsa != resultsDsa)) {
            throw new Exception("km.chooseClientAlias(DsaRsa) failed");
        }
        System.out.println("km.chooseClientAlias(DsaRsa) passed");
        System.out.println("=====");
        resultsNone = km.chooseServerAlias("nothing", null, null);
        if (resultsNone != null) {
            throw new Exception("km.chooseServerAlias(\"nothing\") != null");
        }
        System.out.println("km.ChooseServerAlias(\"nothing\") passed");
        resultsRsa = km.chooseServerAlias("RSA", null, null);
        if (resultsRsa == null)  {
            throw new Exception(
                "km.chooseServerAlias(\"RSA\", null, null) != null");
        }
        System.out.println("km.chooseServerAlias(\"RSA\") passed");
        resultsDsa = km.chooseServerAlias("DSA", null, null);
        if (resultsDsa == null) {
            throw new Exception(
                "km.chooseServerAlias(\"DSA\", null, null) != null");
        }
        System.out.println("km.chooseServerAlias(\"DSA\") passed");
    }
}
