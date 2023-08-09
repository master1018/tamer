public class Crypto extends SecmodTest {
    public static void main(String[] args) throws Exception {
        if (initSecmod() == false) {
            return;
        }
        String configName = BASE + SEP + "nsscrypto.cfg";
        Provider p = getSunPKCS11(configName);
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", p);
        KeyPair kp = kpg.generateKeyPair();
        System.out.println(kp.getPublic());
        System.out.println(kp.getPrivate());
        SecureRandom random = new SecureRandom();
        byte[] data = new byte[2048];
        random.nextBytes(data);
        Signature sig = Signature.getInstance("SHA1withRSA", p);
        sig.initSign(kp.getPrivate());
        sig.update(data);
        byte[] s = sig.sign();
        System.out.println("signature: " + toString(s));
        sig.initVerify(kp.getPublic());
        sig.update(data);
        boolean ok = sig.verify(s);
        if (ok == false) {
            throw new Exception("Signature verification failed");
        }
        System.out.println("OK");
    }
}
