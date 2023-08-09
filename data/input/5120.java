public final class Confounder {
    private static SecureRandom srand = new SecureRandom();
    private Confounder() { 
    }
    public static byte[] bytes(int size) {
        byte[] data = new byte[size];
        srand.nextBytes(data);
        return data;
    }
    public static int intValue() {
        return srand.nextInt();
    }
    public static long longValue() {
        return srand.nextLong();
    }
}
