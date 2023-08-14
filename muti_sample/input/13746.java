public class TestProviderLeak {
    private static void dumpMemoryStats(String s) throws Exception {
        Runtime rt = Runtime.getRuntime();
        System.out.println(s + ":\t" +
            rt.freeMemory() + " bytes free");
    }
    public static void main(String [] args) throws Exception {
        SecretKeyFactory skf =
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1", "SunJCE");
        PBEKeySpec pbeKS = new PBEKeySpec(
            "passPhrase".toCharArray(), new byte [] { 0 }, 5, 512);
        for (int i = 0; i <= 1000; i++) {
            try {
                skf.generateSecret(pbeKS);
                if ((i % 20) == 0) {
                    System.gc();
                    dumpMemoryStats("Iteration " + i);
                }
            } catch (Exception e) {
                dumpMemoryStats("\nException seen at iteration " + i);
                throw e;
            }
        }
    }
}
