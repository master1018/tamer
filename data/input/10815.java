public class TestRSACipherWrap extends PKCS11Test {
    private static final String[] RSA_ALGOS =
        { "RSA/ECB/PKCS1Padding", "RSA" };
    public void main(Provider p) throws Exception {
        try {
            Cipher.getInstance(RSA_ALGOS[0], p);
        } catch (GeneralSecurityException e) {
            System.out.println(RSA_ALGOS[0] + " unsupported, skipping");
            return;
        }
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", p);
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        for (String rsaAlgo: RSA_ALGOS) {
            Cipher cipherPKCS11 = Cipher.getInstance(rsaAlgo, p);
            Cipher cipherJce = Cipher.getInstance(rsaAlgo, "SunJCE");
            String algos[] = {"AES", "RC2", "Blowfish"};
            int keySizes[] = {128, 256};
            for (int j = 0; j < algos.length; j++) {
                String algorithm = algos[j];
                KeyGenerator keygen =
                    KeyGenerator.getInstance(algorithm);
                for (int i = 0; i < keySizes.length; i++) {
                    SecretKey secretKey = null;
                    System.out.print("Generate " + keySizes[i] + "-bit " +
                        algorithm + " key using ");
                    try {
                        keygen.init(keySizes[i]);
                        secretKey = keygen.generateKey();
                        System.out.println(keygen.getProvider().getName());
                    } catch (InvalidParameterException ipe) {
                        secretKey = new SecretKeySpec(new byte[32], algorithm);
                        System.out.println("SecretKeySpec class");
                    }
                    test(kp, secretKey, cipherPKCS11, cipherJce);
                    test(kp, secretKey, cipherPKCS11, cipherPKCS11);
                    test(kp, secretKey, cipherJce, cipherPKCS11);
                }
            }
        }
    }
    private static void test(KeyPair kp, SecretKey secretKey,
            Cipher wrapCipher, Cipher unwrapCipher)
            throws Exception {
        String algo = secretKey.getAlgorithm();
        wrapCipher.init(Cipher.WRAP_MODE, kp.getPublic());
        byte[] wrappedKey = wrapCipher.wrap(secretKey);
        unwrapCipher.init(Cipher.UNWRAP_MODE, kp.getPrivate());
        Key unwrappedKey =
                unwrapCipher.unwrap(wrappedKey, algo, Cipher.SECRET_KEY);
        System.out.println("Test " + wrapCipher.getProvider().getName() +
                "/" + unwrapCipher.getProvider().getName() + ": ");
        if (!Arrays.equals(secretKey.getEncoded(),
                unwrappedKey.getEncoded())) {
            throw new Exception("Test Failed!");
        }
        System.out.println("Passed");
    }
    public static void main(String[] args) throws Exception {
        main(new TestRSACipherWrap());
    }
}
