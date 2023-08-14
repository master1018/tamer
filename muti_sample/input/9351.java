public class ReinitMac extends PKCS11Test {
    public static void main(String[] args) throws Exception {
        main(new ReinitMac());
    }
    public void main(Provider p) throws Exception {
        if (p.getService("Mac", "HmacMD5") == null) {
            System.out.println(p + " does not support HmacMD5, skipping");
            return;
        }
        Random random = new Random();
        byte[] data1 = new byte[10 * 1024];
        random.nextBytes(data1);
        byte[] keyData = new byte[16];
        random.nextBytes(keyData);
        SecretKeySpec key = new SecretKeySpec(keyData, "Hmac");
        Mac mac = Mac.getInstance("HmacMD5", p);
        mac.init(key);
        mac.init(key);
        mac.update(data1);
        mac.init(key);
        mac.doFinal();
        mac.doFinal();
        mac.update(data1);
        mac.doFinal();
        mac.reset();
        mac.reset();
        mac.init(key);
        mac.reset();
        mac.update(data1);
        mac.reset();
        System.out.println("All tests passed");
    }
}
