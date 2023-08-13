public class GenerateRSAKeyPair {
    public static void main(String[] args) throws Exception {
        RSAKeyGenParameterSpec rsaSpec =
        new RSAKeyGenParameterSpec (1024, RSAKeyGenParameterSpec.F4);
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "SunRsaSign");
        kpg.initialize(rsaSpec);
        KeyPair kpair = kpg.generateKeyPair();
        if (kpair == null) {
            throw new Exception("no keypair generated");
        }
    }
}
