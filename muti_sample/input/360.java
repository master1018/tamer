public class NextIntPowerOfTwoMod {
    public static void main(String[] args) throws Exception {
        Random r = new Random(69);
        int total = 0;
        for (int i=0; i<1000; i++)
            total += r.nextInt(16);
        if (total != 7639)
            throw new RuntimeException("Not using correct algorithm.");
    }
}
