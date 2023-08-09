public class PrngSlow {
    public static void main(String[] args) throws Exception {
        double t = 0.0;
        try {
            SecureRandom sr = null;
            sr = SecureRandom.getInstance("PRNG", "SunMSCAPI");
            long start = System.nanoTime();
            int x = 0;
            for(int i = 0; i < 10000; i++) {
                if (i % 100 == 0) System.err.print(".");
                if (sr.nextBoolean()) x++;
            };
            t = (System.nanoTime() - start) / 1000000000.0;
            System.err.println("\nSpend " + t + " seconds");
        } catch (Exception e) {
            System.err.println("Cannot find PRNG for SunMSCAPI or other mysterious bugs");
            e.printStackTrace();
            return;
        }
        if (t > 5)
            throw new RuntimeException("Still too slow");
    }
}
