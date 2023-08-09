public class TestExplicitKeyLength {
    private static final String ALGOS[] = { "RC2", "ARCFOUR" };
    private static final int KEY_SIZES[] =
        { 64, 80 }; 
    public static void runTest(String algo, int keysize) throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(algo, "SunJCE");
        kg.init(keysize);
        Key generatedKey = kg.generateKey();
        int actualSizeInBits = generatedKey.getEncoded().length*8;
        if (actualSizeInBits != keysize) {
            throw new Exception("generated key has wrong length: " +
                                actualSizeInBits + " bits");
        }
    }
    public static void main (String[] args) throws Exception {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        for (int i = 0; i < ALGOS.length; i++) {
            System.out.println("Testing " + ALGOS[i] + " KeyGenerator with " +
                               KEY_SIZES[i] + "-bit keysize");
            runTest(ALGOS[i], KEY_SIZES[i]);
        }
        System.out.println("Tests Passed");
    }
}
