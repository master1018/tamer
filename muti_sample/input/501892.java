@TestTargetClass(targets.MessageDigests.MD2.class)
public class MessageDigestTestMD2 extends TestCase {
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        method = "getInstance",
        args = {String.class}
    )
    @AndroidOnly("Android doesn't include MD2 message digest algorithm")
    public void testMessageDigest1() throws Exception{
        try {
            MessageDigest digest = MessageDigest.getInstance("MD2");
            fail("MD2 MessageDigest algorithm must not be supported");
        } catch (NoSuchAlgorithmException e) {
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(
                    "1.2.840.113549.2.2");
            fail("MD2 MessageDigest algorithm must not be supported");
        } catch (NoSuchAlgorithmException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        method = "getInstance",
        args = {String.class}
    )
    @AndroidOnly("Android allows usage of MD2 in third party providers")
    public void testMessageDigest2() throws Exception{
        Provider provider  = new MyProvider();
        Security.addProvider(provider);
        try {
            MessageDigest digest = MessageDigest.getInstance("MD2");
            digest = MessageDigest.getInstance("1.2.840.113549.2.2");
        } finally {
            Security.removeProvider(provider.getName());
        }
    }
    public final class MyProvider extends Provider {
        public MyProvider() {
            super("MessageDigestMD2Test", 1.00, "TestProvider");
            put("MessageDigest.MD2",
                    "tests.targets.security.MessageDigestTestMD2$MD2");
            put("Alg.Alias.MessageDigest.1.2.840.113549.2.2", "MD2");
        }
    }
    public static class MD2 extends MessageDigest {
        public MD2() {
            super("MD2");
        }
        protected MD2(String algorithm) {
            super(algorithm);
        }
        @Override
        protected byte[] engineDigest() {
            return null;
        }
        @Override
        protected void engineReset() {
        }
        @Override
        protected void engineUpdate(byte input) {
        }
        @Override
        protected void engineUpdate(byte[] input, int offset, int len) {
        }
    }
}
