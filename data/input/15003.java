public class LowercasePermCheck {
    private static String[] ALGOS = {
        "des", "desede", "rsa"
    };
    public static void main(String[] args) throws Exception {
        Provider p = Security.getProvider("SunJCE");
        System.out.println("Testing provider " + p.getName() + "...");
        if (Cipher.getMaxAllowedKeyLength("DES") == Integer.MAX_VALUE) {
            System.out.println("Skip this test due to unlimited version");
            return;
        }
        boolean isFailed = false;
        for (int i = 0; i < ALGOS.length; i++) {
            String algo = ALGOS[i];
            Cipher c = Cipher.getInstance(algo, p);
            int keyLen1 = Cipher.getMaxAllowedKeyLength(algo);
            int keyLen2 = Cipher.getMaxAllowedKeyLength(algo.toUpperCase());
            if (keyLen1 != keyLen2) {
                System.out.println("ERROR: Wrong keysize limit for " + algo);
                System.out.println("Configured: " + keyLen2);
                System.out.println("Actual: " + keyLen1);
                isFailed = true;
            }
            System.out.println(algo + ": max " + keyLen1 + "-bit keys");
        }
        if (isFailed) {
            throw new Exception("Test Failed!");
        } else {
            System.out.println("Test Passed!");
        }
    }
}
