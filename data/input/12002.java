public class TestCurves extends PKCS11Test {
    public static void main(String[] args) throws Exception {
        main(new TestCurves());
    }
    public void main(Provider p) throws Exception {
        if (p.getService("KeyAgreement", "ECDH") == null) {
            System.out.println("Not supported by provider, skipping");
            return;
        }
        Random random = new Random();
        byte[] data = new byte[2048];
        random.nextBytes(data);
        Collection<? extends ECParameterSpec> curves =
            NamedCurve.knownECParameterSpecs();
        for (ECParameterSpec params : curves) {
            System.out.println("Testing " + params + "...");
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", p);
            kpg.initialize(params);
            KeyPair kp1, kp2;
            kp1 = kpg.generateKeyPair();
            kp2 = kpg.generateKeyPair();
            testSigning(p, "SHA1withECDSA", data, kp1, kp2);
            testSigning(p, "SHA256withECDSA", data, kp1, kp2);
            testSigning(p, "SHA384withECDSA", data, kp1, kp2);
            testSigning(p, "SHA512withECDSA", data, kp1, kp2);
            KeyAgreement ka1 = KeyAgreement.getInstance("ECDH", p);
            ka1.init(kp1.getPrivate());
            ka1.doPhase(kp2.getPublic(), true);
            byte[] secret1 = ka1.generateSecret();
            KeyAgreement ka2 = KeyAgreement.getInstance("ECDH", p);
            ka2.init(kp2.getPrivate());
            ka2.doPhase(kp1.getPublic(), true);
            byte[] secret2 = ka2.generateSecret();
            if (Arrays.equals(secret1, secret2) == false) {
                throw new Exception("Secrets do not match");
            }
        }
        System.out.println("OK");
    }
    private static void testSigning(Provider p, String algorithm,
            byte[] data, KeyPair kp1, KeyPair kp2) throws Exception {
        Signature s = Signature.getInstance(algorithm, p);
        s.initSign(kp1.getPrivate());
        s.update(data);
        byte[] sig = s.sign();
        s = Signature.getInstance(algorithm, p);
        s.initVerify(kp1.getPublic());
        s.update(data);
        boolean r = s.verify(sig);
        if (r == false) {
            throw new Exception("Signature did not verify");
        }
        s.initVerify(kp2.getPublic());
        s.update(data);
        r = s.verify(sig);
        if (r) {
            throw new Exception("Signature should not verify");
        }
    }
}
