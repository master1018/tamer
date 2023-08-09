public class RC4AliasPermCheck {
    private static void test(String algo, int keyLen) throws Exception {
        Provider p = Security.getProvider("SunJCE");
        System.out.println("=>Testing " + algo + " cipher with "
                           + keyLen + "-bit key");
        KeyGenerator kg = KeyGenerator.getInstance(algo, p);
        kg.init(keyLen);
        SecretKey key = kg.generateKey();
        System.out.println("Generated key with algorithm " +
                           key.getAlgorithm());
        Cipher cipher = Cipher.getInstance(algo, p);
        System.out.println("Requested cipher with algorithm " +
                           algo);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        System.out.println("Initialization succeeded as expected");
    }
    public static void main(String[] argv) throws Exception {
        test("ARCFOUR", 120);
        test("RC4", 120);
        System.out.println("TEST PASSED");
    }
}
