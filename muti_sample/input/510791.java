public class MyKeyPairGenerator3 extends KeyPairGenerator {
    public MyKeyPairGenerator3() {
        super("KPGen_3");
    }
    public MyKeyPairGenerator3(String s) {
        super(s);
    }
    public KeyPair generateKeyPair() {
        PublicKey pubK = (new MyKeyPairGenerator1()).new PubKey();
        PrivateKey priK = (new MyKeyPairGenerator1()).new PrivKey();
        return new KeyPair(pubK, priK);
    }
}
