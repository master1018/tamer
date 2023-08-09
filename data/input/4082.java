public class SelfSeed {
    private static final int NUM_BYTES = 5;
    private static byte seed[] = { (byte)0xaa, (byte)0x11, (byte)0xa1 };
    public static void main(String[] args) {
        try {
            SecureRandom sr1 = SecureRandom.getInstance("SHA1PRNG");
            sr1.setSeed(seed);
            byte randomBytes[] = new byte[NUM_BYTES];
            sr1.nextBytes(randomBytes);
            SecureRandom sr2 = new SecureRandom(seed);
            if (sr2.getAlgorithm().equals("SHA1PRNG") == false) {
                System.out.println("Default PRNG is not SHA1PRNG, skipping test");
                return;
            }
            byte otherRandomBytes[] = new byte[NUM_BYTES];
            sr2.nextBytes(otherRandomBytes);
            for (int i = 0; i < NUM_BYTES; i++) {
                if (randomBytes[i] != otherRandomBytes[i])
                    throw new SecurityException("FAILURE: " +
                                        "Returned bytes not equal");
            }
        } catch (Exception e) {
            throw new SecurityException("FAILURE: " + e.toString());
        }
    }
}
