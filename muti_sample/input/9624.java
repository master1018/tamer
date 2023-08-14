public class ReinitDigest extends PKCS11Test {
    public static void main(String[] args) throws Exception {
        main(new ReinitDigest());
    }
    public void main(Provider p) throws Exception {
        if (p.getService("MessageDigest", "MD5") == null) {
            System.out.println("Provider does not support MD5, skipping");
            return;
        }
        Random r = new Random();
        byte[] data1 = new byte[10 * 1024];
        byte[] data2 = new byte[10 * 1024];
        r.nextBytes(data1);
        r.nextBytes(data2);
        MessageDigest md;
        md = MessageDigest.getInstance("MD5", "SUN");
        byte[] d1 = md.digest(data1);
        md = MessageDigest.getInstance("MD5", p);
        byte[] d2 = md.digest(data1);
        check(d1, d2);
        byte[] d3 = md.digest(data1);
        check(d1, d3);
        md.update(data2);
        md.update((byte)0);
        md.reset();
        byte[] d4 = md.digest(data1);
        check(d1, d4);
        System.out.println("All tests passed");
    }
    private static void check(byte[] d1, byte[] d2) throws Exception {
        if (Arrays.equals(d1, d2) == false) {
            throw new RuntimeException("Digest mismatch");
        }
    }
}
