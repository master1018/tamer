public class TestGetInstance {
    private static void same(Object o1, Object o2) throws Exception {
        if (o1 != o2) {
            throw new Exception("not same object");
        }
    }
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        Provider p = Security.getProvider("SunJCE");
        Mac mac;
        mac = Mac.getInstance("hmacmd5");
        System.out.println("Default: " + mac.getProvider().getName());
        mac = Mac.getInstance("hmacmd5", "SunJCE");
        same(p, mac.getProvider());
        mac = Mac.getInstance("hmacmd5", p);
        same(p, mac.getProvider());
        try {
            mac = Mac.getInstance("foo");
            throw new AssertionError();
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }
        try {
            mac = Mac.getInstance("foo", "SunJCE");
            throw new AssertionError();
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }
        try {
            mac = Mac.getInstance("foo", p);
            throw new AssertionError();
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }
        try {
            mac = Mac.getInstance("foo", "SUN");
            throw new AssertionError();
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }
        try {
            mac = Mac.getInstance("foo", Security.getProvider("SUN"));
            throw new AssertionError();
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }
        try {
            mac = Mac.getInstance("foo", "foo");
            throw new AssertionError();
        } catch (NoSuchProviderException e) {
            System.out.println(e);
        }
        long stop = System.currentTimeMillis();
        System.out.println("Done (" + (stop - start) + " ms).");
    }
}
