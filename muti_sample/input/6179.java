public class TestKeyPairGeneratorLength {
    public static void main(String[] args) throws Exception {
        test(512);
        test(513);
        System.out.println("Done.");
    }
    private static void test(int len) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "SunRsaSign");
        kpg.initialize(len);
        for (int i = 0; i < 6; i++) {
            System.out.println("Generating keypair " + len + " bit keypair " + (i + 1) + "...");
            KeyPair kp = kpg.generateKeyPair();
            RSAPublicKey key = (RSAPublicKey)kp.getPublic();
            int k = key.getModulus().bitLength();
            if (k != len) {
                throw new Exception("length mismatch: " + k);
            }
        }
    }
}
