public class TestDH extends PKCS11Test {
    public void main(Provider p) throws Exception {
        if (p.getService("KeyAgreement", "DH") == null) {
            System.out.println("DH not supported, skipping");
            return;
        }
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH", p);
        kpg.initialize(512);
        KeyPair kp1 = kpg.generateKeyPair();
        KeyPair kp2 = kpg.generateKeyPair();
        KeyAgreement ka1, ka2;
        ka1 = KeyAgreement.getInstance("DH", p);
        ka1.init(kp1.getPrivate());
        ka1.doPhase(kp2.getPublic(), true);
        System.out.println("Derive 1...");
        byte[] secret1 = ka1.generateSecret();
        ka1.init(kp2.getPrivate());
        ka1.doPhase(kp1.getPublic(), true);
        System.out.println("Derive 2...");
        byte[] secret2 = ka1.generateSecret();
        if (Arrays.equals(secret1, secret2) == false) {
            throw new Exception("Secrets (1,2) do not match");
        }
        ka2 = KeyAgreement.getInstance("DH", "SunJCE");
        ka2.init(kp1.getPrivate());
        ka2.doPhase(kp2.getPublic(), true);
        System.out.println("Derive 3...");
        byte[] secret3 = ka2.generateSecret();
        if (Arrays.equals(secret1, secret3) == false) {
            throw new Exception("Secrets (1,3) do not match");
        }
        ka2.init(kp2.getPrivate());
        ka2.doPhase(kp1.getPublic(), true);
        System.out.println("Derive 4...");
        byte[] secret4 = ka2.generateSecret();
        if (Arrays.equals(secret1, secret4) == false) {
            throw new Exception("Secrets (1,4) do not match");
        }
        testAlgorithm(ka2, kp2, ka1, kp1, "DES");
        testAlgorithm(ka2, kp2, ka1, kp1, "DESede");
        testAlgorithm(ka2, kp2, ka1, kp1, "Blowfish");
        testAlgorithm(ka2, kp2, ka1, kp1, "TlsPremasterSecret");
    }
    private static void testAlgorithm(KeyAgreement ka1, KeyPair kp1, KeyAgreement ka2, KeyPair kp2, String algorithm) throws Exception {
        SecretKey key1 = null;
        ka1.init(kp1.getPrivate());
        ka1.doPhase(kp2.getPublic(), true);
        System.out.println("Derive " + algorithm + " using SunJCE...");
        key1 = ka1.generateSecret(algorithm);
        ka2.init(kp1.getPrivate());
        ka2.doPhase(kp2.getPublic(), true);
        System.out.println("Derive " + algorithm + " using PKCS#11...");
        SecretKey key2 = ka2.generateSecret(algorithm);
        byte[] b1 = key1.getEncoded();
        byte[] b2 = key2.getEncoded();
        if (Arrays.equals(b1, b2) == false) {
            System.out.println(b1.length + " bytes: " + toString(b1));
            System.out.println(b2.length + " bytes: " + toString(b2));
            throw new Exception(algorithm + " secret mismatch");
        }
    }
    public static void main(String[] args) throws Exception {
        main(new TestDH());
    }
}
