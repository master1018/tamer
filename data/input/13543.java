public class GenerateKeypair {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(512);
        KeyPair kpair = kpg.generateKeyPair();
        if (kpair == null) {
            throw new Exception("no keypair generated");
        }
        kpair = kpg.genKeyPair();
        if (kpair == null) {
            throw new Exception("no keypair generated");
        }
    }
}
