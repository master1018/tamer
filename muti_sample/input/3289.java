public class NONEwithRSA {
    public static void main(String[] args) throws Exception {
        Random random = new Random();
        byte[] b = new byte[16];
        random.nextBytes(b);
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();
        Signature sig = Signature.getInstance("NONEwithRSA");
        sig.initSign(kp.getPrivate());
        System.out.println("Provider: " + sig.getProvider());
        sig.update(b);
        byte[] sb = sig.sign();
        sig.initVerify(kp.getPublic());
        sig.update(b);
        if (sig.verify(sb) == false) {
            throw new Exception("verification failed");
        }
        Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        c.init(Cipher.DECRYPT_MODE, kp.getPublic());
        byte[] dec = c.doFinal(sb);
        if (Arrays.equals(dec, b) == false) {
            throw new Exception("decryption failed");
        }
        sig = Signature.getInstance("NONEwithRSA", "SunJCE");
        sig.initSign(kp.getPrivate());
        sig = Signature.getInstance("NONEwithRSA", Security.getProvider("SunJCE"));
        sig.initSign(kp.getPrivate());
        try {
            Signature.getInstance("NONEwithRSA", "SUN");
            throw new Exception("call succeeded");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        System.out.println("OK");
    }
    private static void showProvider(Provider p) {
        System.out.println(p);
        for (Iterator t = p.getServices().iterator(); t.hasNext(); ) {
            System.out.println(t.next());
        }
    }
}
