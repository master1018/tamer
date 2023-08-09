public class AllPermCheck {
    private static String SYM_ALGOS[] = {
        "AES", "Blowfish", "RC2", "ARCFOUR"
    };
    public static void runTest(Cipher c, Key key) throws Exception {
        SecureRandom sr = new SecureRandom();
        for (int i = 0; i < 6; i++) {
            try {
                switch (i) {
                case 0:
                    c.init(Cipher.ENCRYPT_MODE, key);
                    break;
                case 1:
                    c.init(Cipher.ENCRYPT_MODE, key, sr);
                    break;
                case 2:
                    c.init(Cipher.ENCRYPT_MODE, key,
                           (AlgorithmParameters)null);
                    break;
                case 3:
                    c.init(Cipher.ENCRYPT_MODE, key,
                           (AlgorithmParameters)null, sr);
                    break;
                case 4:
                    c.init(Cipher.ENCRYPT_MODE, key,
                           (AlgorithmParameterSpec)null);
                    break;
                case 5:
                    c.init(Cipher.ENCRYPT_MODE, key,
                           (AlgorithmParameterSpec)null, sr);
                    break;
                }
                throw new Exception("...#" + i + " should throw IKE for " +
                                    key.getEncoded().length + "-byte keys");
            } catch (InvalidKeyException ike) {
                System.out.println("...#" + i + " expected IKE thrown");
            }
        }
    }
    public static void main(String[] args) throws Exception {
        Provider p = Security.getProvider("SunJCE");
        System.out.println("Testing provider " + p.getName() + "...");
        if (Cipher.getMaxAllowedKeyLength("DES") == Integer.MAX_VALUE) {
            System.out.println("Skip this test due to unlimited version");
            return;
        }
        for (int i = 0; i < SYM_ALGOS.length; i++) {
            String algo = SYM_ALGOS[i];
            Cipher c = Cipher.getInstance(algo, p);
            int keyLength = Cipher.getMaxAllowedKeyLength(algo);
            SecretKey key = new SecretKeySpec(new byte[keyLength/8 + 8], algo);
            System.out.println("Testing " + algo + " Cipher");
            runTest(c, key);
        }
        System.out.println("All tests passed!");
    }
}
