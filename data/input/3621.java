public class GetAlgorithm {
    private final static String BASE = System.getProperty("test.src", ".");
    public static void main(String[] args) throws Exception {
        SecureRandom sr;
        sr = new SecureRandom();
        if (sr.getAlgorithm().equals("unknown")) {
            throw new Exception("Unknown: " + sr.getAlgorithm());
        }
        sr = SecureRandom.getInstance("SHA1PRNG");
        check("SHA1PRNG", sr);
        sr = new MySecureRandom();
        check("unknown", sr);
        InputStream in = new FileInputStream(new File(BASE, "sha1prng-old.bin"));
        ObjectInputStream oin = new ObjectInputStream(in);
        sr = (SecureRandom)oin.readObject();
        oin.close();
        check("unknown", sr);
        in = new FileInputStream(new File(BASE, "sha1prng-new.bin"));
        oin = new ObjectInputStream(in);
        sr = (SecureRandom)oin.readObject();
        oin.close();
        check("SHA1PRNG", sr);
        System.out.println("All tests passed");
    }
    private static void check(String s1, SecureRandom sr) throws Exception {
        String s2 = sr.getAlgorithm();
        if (s1.equals(s2) == false) {
            throw new Exception("Expected " + s1 + ", got " + s2);
        }
    }
    private static class MySecureRandom extends SecureRandom {
    }
}
