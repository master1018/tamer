public class PBEInvalidParamsTest {
    private static final char[] PASSWORD = { 'p', 'a', 's', 's' };
    private static final String[] PBE_ALGOS = {
        "PBEWithMD5AndDES", "PBEWithSHA1AndDESede", "PBEWithSHA1AndRC2_40"
    };
    private static final IvParameterSpec INVALID_PARAMS =
        new IvParameterSpec(new byte[8]);
    public static void main(String[] args) throws Exception {
        PBEKeySpec ks = new PBEKeySpec(PASSWORD);
        for (int i = 0; i < PBE_ALGOS.length; i++) {
            String algo = PBE_ALGOS[i];
            System.out.println("=>testing " + algo);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(algo);
            SecretKey key = skf.generateSecret(ks);
            Cipher c = Cipher.getInstance(algo, "SunJCE");
            try {
                c.init(Cipher.ENCRYPT_MODE, key, INVALID_PARAMS);
                throw new Exception("Test Failed: expected IAPE is " +
                                     "not thrown for " + algo);
            } catch (InvalidAlgorithmParameterException iape) {
                continue;
            }
        }
        System.out.println("Test Passed");
    }
}
