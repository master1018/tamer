public class TestECGenSpec extends PKCS11Test {
    public static void main(String[] args) throws Exception {
        main(new TestECGenSpec());
    }
    public void main(Provider p) throws Exception {
        if (p.getService("Signature", "SHA1withECDSA") == null) {
            System.out.println("Provider does not support ECDSA, skipping...");
            return;
        }
        String[] names = { "NIST P-192", "sect163k1", "1.3.132.0.26", "X9.62 c2tnb239v1"};
        int[] lengths = {192, 163, 233, 239};
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            int len = lengths[i];
            System.out.println("Testing " + name + "...");
            ECGenParameterSpec spec = new ECGenParameterSpec(name);
            AlgorithmParameters algParams = AlgorithmParameters.getInstance("EC", p);
            algParams.init(spec);
            ECParameterSpec ecSpec = algParams.getParameterSpec(ECParameterSpec.class);
            System.out.println(ecSpec);
            if (ecSpec.toString().contains(name) == false) {
                throw new Exception("wrong curve");
            }
            algParams = AlgorithmParameters.getInstance("EC", p);
            algParams.init(ecSpec);
            ECGenParameterSpec genSpec = algParams.getParameterSpec(ECGenParameterSpec.class);
            System.out.println(genSpec.getName());
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", p);
            kpg.initialize(spec);
            KeyPair kp = kpg.generateKeyPair();
            System.out.println(kp.getPrivate());
            ECPublicKey publicKey = (ECPublicKey)kp.getPublic();
            if (publicKey.getParams().getCurve().getField().getFieldSize() != len) {
                throw new Exception("wrong curve");
            }
            System.out.println();
        }
    }
}
