public class TestRSAKeyLength extends PKCS11Test {
    public static void main(String[] args) throws Exception {
        main(new TestRSAKeyLength());
    }
    public void main(Provider p) throws Exception {
        boolean isValidKeyLength[] = { true, true, false, false };
        String algos[] = { "SHA1withRSA", "SHA256withRSA",
                           "SHA384withRSA", "SHA512withRSA" };
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", p);
        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();
        PrivateKey privKey = kp.getPrivate();
        PublicKey pubKey = kp.getPublic();
        for (int i = 0; i < algos.length; i++) {
            Signature sig = Signature.getInstance(algos[i], p);
            System.out.println("Testing RSA signature " + algos[i]);
            try {
                sig.initSign(privKey);
                if (!isValidKeyLength[i]) {
                    throw new Exception("initSign: Expected IKE not thrown!");
                }
            } catch (InvalidKeyException ike) {
                if (isValidKeyLength[i]) {
                    throw new Exception("initSign: Unexpected " + ike);
                }
            }
            try {
                sig.initVerify(pubKey);
                if (!isValidKeyLength[i]) {
                    throw new RuntimeException("initVerify: Expected IKE not thrown!");
                }
                new SignedObject("Test string for getSignature test.", privKey, sig);
            } catch (InvalidKeyException ike) {
                if (isValidKeyLength[i]) {
                    throw new Exception("initSign: Unexpected " + ike);
                }
            }
        }
    }
}
