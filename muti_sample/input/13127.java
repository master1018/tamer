public class TestJKSWithSecretKey {
    private static char[] passwd = { 'p','a','s','s','w','d'};
    public static void main (String[] args) throws Exception {
        SecretKey key = new SecretKeySpec(new byte[8], "DES");
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, passwd);
        try {
            ks.setKeyEntry("test_encrypt_key", key, passwd, null);
            throw new Exception("Should throw KeyStoreException when " +
                "storing SecretKey into JKS keystores");
        } catch (KeyStoreException kse) {
        }
    }
}
