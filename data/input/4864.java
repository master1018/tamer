public class ModPow65537 {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "SunRsaSign");
        kpg.initialize(new RSAKeyGenParameterSpec(512, BigInteger.valueOf(65537)));
        KeyPair kp = kpg.generateKeyPair();
        testSigning(kp);
        kpg.initialize(new RSAKeyGenParameterSpec(512, BigInteger.valueOf(65539)));
        kp = kpg.generateKeyPair();
        testSigning(kp);
        kpg.initialize(new RSAKeyGenParameterSpec(512, BigInteger.valueOf(3)));
        kp = kpg.generateKeyPair();
        testSigning(kp);
        BigInteger base = new BigInteger("19058071224156864789844466979330892664777520457048234786139035643344145635582");
        BigInteger mod  = new BigInteger("75554098474976067521257305210610421240510163914613117319380559667371251381587");
        BigInteger exp1 = BigInteger.valueOf(65537);
        BigInteger exp2 = BigInteger.valueOf(75537);
        BigInteger exp3 = new BigInteger("13456870775607312149");
        BigInteger res1 = new BigInteger("5770048609366563851320890693196148833634112303472168971638730461010114147506");
        BigInteger res2 = new BigInteger("63446979364051087123350579021875958137036620431381329472348116892915461751531");
        BigInteger res3 = new BigInteger("39016891919893878823999350081191675846357272199067075794096200770872982089502");
        if (base.modPow(exp1, mod).equals(res1) == false) {
            throw new Exception("Error using " + exp1);
        }
        if (base.modPow(exp2, mod).equals(res2) == false) {
            throw new Exception("Error using " + exp2);
        }
        if (base.modPow(exp3, mod).equals(res3) == false) {
            throw new Exception("Error using " + exp3);
        }
        System.out.println("Passed");
    }
    private static void testSigning(KeyPair kp) throws Exception {
        System.out.println(kp.getPublic());
        byte[] data = new byte[1024];
        new Random().nextBytes(data);
        Signature sig = Signature.getInstance("SHA1withRSA", "SunRsaSign");
        sig.initSign(kp.getPrivate());
        sig.update(data);
        byte[] sigBytes = sig.sign();
        sig.initVerify(kp.getPublic());
        sig.update(data);
        if (sig.verify(sigBytes) == false) {
            throw new Exception("signature verification failed");
        }
        System.out.println("OK");
    }
}
