public class KeyWrap extends PKCS11Test {
    public void main(Provider p) throws Exception {
        try {
            Cipher.getInstance("RSA/ECB/PKCS1Padding", p);
        } catch (GeneralSecurityException e) {
            System.out.println("Not supported by provider, skipping");
            return;
        }
        KeyPair kp;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", p);
            kpg.initialize(512);
            kp = kpg.generateKeyPair();
        } catch (Exception e) {
            try {
                System.out.println("Could not generate KeyPair on provider " + p + ", trying migration");
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                kpg.initialize(512);
                kp = kpg.generateKeyPair();
                KeyFactory kf = KeyFactory.getInstance("RSA", p);
                PublicKey pub = (PublicKey)kf.translateKey(kp.getPublic());
                PrivateKey priv = (PrivateKey)kf.translateKey(kp.getPrivate());
                kp = new KeyPair(pub, priv);
            } catch (Exception ee) {
                ee.printStackTrace();
                System.out.println("Provider does not support RSA, skipping");
                return;
            }
        }
        System.out.println(kp);
        Random r = new Random();
        byte[] b = new byte[16];
        r.nextBytes(b);
        String alg = "AES";
        SecretKey key = new SecretKeySpec(b, alg);
        Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding", p);
        c.init(Cipher.WRAP_MODE, kp.getPublic());
        byte[] wrapped = c.wrap(key);
        System.out.println("wrapped: " + wrapped.length);
        c.init(Cipher.UNWRAP_MODE, kp.getPrivate());
        Key unwrapped = c.unwrap(wrapped, alg, Cipher.SECRET_KEY);
        System.out.println("unwrapped: " + unwrapped);
        boolean eq = key.equals(unwrapped);
        System.out.println(eq);
        if (eq == false) {
            throw new Exception("Unwrapped key does not match original key");
        }
    }
    public static void main(String[] args) throws Exception {
        main(new KeyWrap());
    }
}
