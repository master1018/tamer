public class TestInitSignWithMyOwnRandom {
    public static void main(String[] argv) throws Exception {
        Provider p = Security.getProvider("SUN");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA", p);
        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();
        TestRandomSource rand = new TestRandomSource();
        Signature sig = Signature.getInstance("DSA", p);
        sig.initSign(kp.getPrivate(), rand);
        sig.update(new byte[20]);
        sig.sign();
        if (rand.isUsed()) {
            System.out.println("Custom random source is used.");
        } else {
            throw new Exception("Custom random source is not used");
        }
    }
}
class TestRandomSource extends SecureRandom {
    int count = 0;
    public int nextInt() {
        count++;
        return 0;
    }
    public boolean isUsed() {
        return (count != 0);
    }
}
