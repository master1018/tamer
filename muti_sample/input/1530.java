public class TestRawRSACipher extends PKCS11Test {
    public void main(Provider p) throws Exception {
        try {
            Cipher.getInstance("RSA/ECB/NoPadding", p);
        } catch (GeneralSecurityException e) {
            System.out.println("Not supported by provider, skipping");
            return;
        }
        final int KEY_LEN = 1024;
        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA", p);
        kpGen.initialize(KEY_LEN);
        KeyPair kp = kpGen.generateKeyPair();
        Random random = new Random();
        byte[] plainText, cipherText, recoveredText;
        plainText = new byte[KEY_LEN/8];
        random.nextBytes(plainText);
        plainText[0] = 0; 
        Cipher c1 = Cipher.getInstance("RSA/ECB/NoPadding", p);
        Cipher c2 = Cipher.getInstance("RSA/ECB/NoPadding", "SunJCE");
        c1.init(Cipher.ENCRYPT_MODE, kp.getPublic());
        c2.init(Cipher.DECRYPT_MODE, kp.getPrivate());
        cipherText = c1.doFinal(plainText);
        recoveredText = c2.doFinal(cipherText);
        if (!Arrays.equals(plainText, recoveredText)) {
            throw new RuntimeException("E/D Test against SunJCE Failed!");
        }
        c2.init(Cipher.ENCRYPT_MODE, kp.getPublic());
        c1.init(Cipher.DECRYPT_MODE, kp.getPrivate());
        cipherText = c2.doFinal(plainText);
        recoveredText = c1.doFinal(cipherText);
        if (!Arrays.equals(plainText, recoveredText)) {
            throw new RuntimeException("D/E Test against SunJCE Failed!");
        }
        System.out.println("Test Passed");
    }
    public static void main(String[] args) throws Exception {
        main(new TestRawRSACipher());
    }
}
