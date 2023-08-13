public class ReinitSignature extends PKCS11Test {
    public static void main(String[] args) throws Exception {
        main(new ReinitSignature());
    }
    public void main(Provider p) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", p);
        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();
        PrivateKey privateKey = kp.getPrivate();
        PublicKey publicKey = kp.getPublic();
        Signature sig = Signature.getInstance("MD5withRSA", p);
        byte[] data = new byte[10 * 1024];
        new Random().nextBytes(data);
        sig.initSign(privateKey);
        sig.initSign(privateKey);
        sig.update(data);
        sig.initSign(privateKey);
        sig.update(data);
        byte[] signature = sig.sign();
        sig.update(data);
        sig.initSign(privateKey);
        sig.update(data);
        sig.sign();
        sig.sign();
        sig.initSign(privateKey);
        sig.sign();
        System.out.println("All tests passed");
    }
}
