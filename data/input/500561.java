@TestTargetClass(targets.Signatures.MD2withRSA.class)
public class SignatureTestMD2withRSA extends TestCase {
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        method = "getInstance",
        args = {String.class}
    )
    @AndroidOnly("Android doesn't include MD2withRSA signature algorithm")
    public void testSignature() {
        try {
            Signature signature = Signature.getInstance("MD2withRSA");
            fail("MD2withRSA for signature verification must not be supported");
        } catch (NoSuchAlgorithmException e) {
        }
        try {
            Signature signature = Signature.getInstance("MD2WithRSA");
            fail("MD2withRSA for signature verification must not be supported");
        } catch (NoSuchAlgorithmException e) {
        }
        try {
            Signature signature = Signature.getInstance("MD2WITHRSA");
            fail("MD2withRSA for signature verification must not be supported");
        } catch (NoSuchAlgorithmException e) {
        }
        try {
            Signature signature = Signature.getInstance("MD2withRSAEncryption");
            fail("MD2withRSA for signature verification must not be supported");
        } catch (NoSuchAlgorithmException e) {
        }
        try {
            Signature signature = Signature.getInstance("MD2WithRSAEncryption");
            fail("MD2withRSA for signature verification must not be supported");
        } catch (NoSuchAlgorithmException e) {
        }
        try {
            Signature signature = Signature.getInstance("MD2WITHRSAENCRYPTION");
            fail("MD2withRSA for signature verification must not be supported");
        } catch (NoSuchAlgorithmException e) {
        }
        try {
            Signature signature = Signature.getInstance("MD2/RSA");
            fail("MD2withRSA for signature verification must not be supported");
        } catch (NoSuchAlgorithmException e) {
        }
        try {
            Signature signature = Signature.getInstance("1.2.840.113549.1.1.2");
            fail("MD2withRSA for signature verification must not be supported");
        } catch (NoSuchAlgorithmException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        method = "getInstance",
        args = {String.class}
    )
    @AndroidOnly("Android allows usage of MD2withRSA in third party providers")
    public void testSignature2() throws Exception{
        Security.addProvider(new MyProvider());
        Signature signature = Signature.getInstance("MD2withRSA");
        signature = Signature.getInstance("MD2WithRSA");
        signature = Signature.getInstance("MD2WITHRSA");
        signature = Signature.getInstance("MD2withRSAEncryption");
        signature = Signature.getInstance("MD2WithRSAEncryption");
        signature = Signature.getInstance("MD2WITHRSAENCRYPTION");
        signature = Signature.getInstance("MD2/RSA");
        signature = Signature.getInstance("1.2.840.113549.1.1.2");
    }
    public final class MyProvider extends Provider {
        public MyProvider()
        {
            super("SignatureMD2WithRSATest", 1.00, "TestProvider");
            put("Signature.MD2WithRSAEncryption",
                    "tests.targets.security.SignatureTestMD2withRSA$MD2withRSA");
            put("Alg.Alias.Signature.MD2WITHRSAENCRYPTION",
                    "MD2WithRSAEncryption");
            put("Alg.Alias.Signature.MD2WithRSA", "MD2WithRSAEncryption");
            put("Alg.Alias.Signature.MD2withRSA", "MD2WithRSAEncryption");
            put("Alg.Alias.Signature.MD2/RSA", "MD2WithRSAEncryption");
            put("Alg.Alias.Signature.1.2.840.113549.1.1.2",
                    "MD2WithRSAEncryption");
        }
    }
    public static class MD2withRSA extends Signature {
        public MD2withRSA() {
            super("MD2WithRSA");
        }
        public MD2withRSA(String algorithm) {
            super(algorithm);
        }
        @Override
        protected Object engineGetParameter(String param)
                throws InvalidParameterException {
            return null;
        }
        @Override
        protected void engineInitSign(PrivateKey privateKey)
                throws InvalidKeyException {
        }
        @Override
        protected void engineInitVerify(PublicKey publicKey)
                throws InvalidKeyException {
        }
        @Override
        protected void engineSetParameter(String param, Object value)
                throws InvalidParameterException {
        }
        @Override
        protected byte[] engineSign() throws SignatureException {
            return null;
        }
        @Override
        protected void engineUpdate(byte b) throws SignatureException {
        }
        @Override
        protected void engineUpdate(byte[] b, int off, int len)
                throws SignatureException {
        }
        @Override
        protected boolean engineVerify(byte[] sigBytes)
                throws SignatureException {
            return false;
        }
    }
}
