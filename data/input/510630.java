@TestTargetClass(EncodedKeySpec.class)
public class EncodedKeySpec2Test extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getEncoded",
        args = {}
    )
    public void test_getEncoded() throws Exception {
               KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
               keyGen.initialize(1024);
               KeyPair keys = keyGen.generateKeyPair();
               KeyFactory fact = KeyFactory.getInstance("DSA");
               byte[] encoded = keys.getPublic().getEncoded();
               Key key = fact.generatePublic(new X509EncodedKeySpec(encoded));
               assertTrue("public key encodings were different", 
                           isEqual(key, keys.getPublic()));
               encoded = keys.getPrivate().getEncoded();
               key = fact.generatePrivate(new PKCS8EncodedKeySpec(encoded));
               assertTrue("private key encodings were different",
                           isEqual(key, keys.getPrivate()));
    }
    private boolean isEqual(Key key1, Key key2) {
        if (key1 instanceof DSAPublicKey && key2 instanceof DSAPublicKey) {
            DSAPublicKey dsa1 = ((DSAPublicKey) key1);
            DSAPublicKey dsa2 = ((DSAPublicKey) key2);
            return dsa1.getY().equals(dsa2.getY())
                    && dsa1.getParams().getG().equals(dsa2.getParams().getG())
                    && dsa1.getParams().getP().equals(dsa2.getParams().getP())
                    && dsa1.getParams().getQ().equals(dsa2.getParams().getQ());
        } else if (key1 instanceof DSAPrivateKey
                && key2 instanceof DSAPrivateKey) {
            DSAPrivateKey dsa1 = ((DSAPrivateKey) key1);
            DSAPrivateKey dsa2 = ((DSAPrivateKey) key2);
            return dsa1.getX().equals(dsa2.getX())
                    && dsa1.getParams().getG().equals(dsa2.getParams().getG())
                    && dsa1.getParams().getP().equals(dsa2.getParams().getP())
                    && dsa1.getParams().getQ().equals(dsa2.getParams().getQ());
        } else {
            return false;
        }
    }
}
