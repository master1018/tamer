public class HmacPBESHA1 {
    private static final String MAC_ALGO = "HmacPBESHA1";
    private static final String KEY_ALGO = "PBE";
    private static final String PROVIDER = "SunJCE";
    private SecretKey key = null;
    public static void main(String argv[]) throws Exception {
        HmacPBESHA1 test = new HmacPBESHA1();
        test.run();
        System.out.println("Test Passed");
    }
    public void run() throws Exception {
        if (key == null) {
            char[] password = { 't', 'e', 's', 't' };
            PBEKeySpec keySpec = new PBEKeySpec(password);
            SecretKeyFactory kf = SecretKeyFactory.getInstance(KEY_ALGO, PROVIDER);
            key = kf.generateSecret(keySpec);
        }
        Mac mac = Mac.getInstance(MAC_ALGO, PROVIDER);
        byte[] plainText = new byte[30];
        mac.init(key);
        mac.update(plainText);
        byte[] value1 = mac.doFinal();
        if (value1.length != 20) {
            throw new Exception("incorrect MAC output length, " +
                                "expected 20, got " + value1.length);
        }
        mac.update(plainText);
        byte[] value2 = mac.doFinal();
        if (!Arrays.equals(value1, value2)) {
            throw new Exception("generated different MAC outputs");
        }
    }
}
