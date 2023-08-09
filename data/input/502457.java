@TestTargetClass(Signature.class)
public class Signature2Test extends junit.framework.TestCase {
    private static final String MESSAGE = "abc";
    static KeyPair dsaKeys;
    static KeyPair rsaKeys;
    static {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
            keyGen.initialize(1024);
            dsaKeys = keyGen.generateKeyPair();
            KeyPairGenerator keyGen2 = KeyPairGenerator.getInstance("RSA");
            keyGen2.initialize(1024);
            rsaKeys = keyGen2.generateKeyPair();
        } catch (Exception e) {
            fail(e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "CloneNotSupportedException checking was tested",
        method = "clone",
        args = {}
    )
    public void test_clone() throws Exception {
        Signature s = Signature.getInstance("DSA");
        try {
            s.clone();
            fail("A Signature may not be cloneable");
        } catch (CloneNotSupportedException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgorithm",
        args = {}
    )
    public void test_getAlgorithm() throws Exception {
        String alg = Signature.getInstance("DSA").getAlgorithm();
        assertTrue("getAlgorithm did not get DSA (" + alg + ")", alg
                .indexOf("DSA") != -1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_String() {
        try {
            Signature.getInstance("DSA");
        } catch (Exception e) {
            fail("Unexpected exception for DSA algorithm");
        }
        try {
            Signature.getInstance("SHA-256");
            fail("NoSuchAlgorithmException was not thrown for unavailable algorithm");
        } catch (NoSuchAlgorithmException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_lang_String_java_security_Provider()
            throws Exception {
        Provider[] providers = Security.getProviders("Signature.DSA");
        for (int i = 0; i < providers.length; i++) {
            Signature signature = Signature.getInstance("DSA", providers[i]);
            assertEquals("DSA", signature.getAlgorithm());
            assertEquals(providers[i], signature.getProvider());
        }
        try {
            Signature.getInstance((String) null, (Provider) null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
        try {
            Signature.getInstance("DSA", (Provider) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            Signature.getInstance((String) null, providers[0]);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
        try {
            Signature.getInstance("SHA-256", providers[0]);
            fail("NoSuchAlgorithmException expected");
        } catch (NoSuchAlgorithmException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_lang_String()
            throws Exception {
        Provider[] providers = Security.getProviders("Signature.DSA");
        for (int i = 0; i < providers.length; i++) {
            Signature.getInstance("DSA", providers[i].getName());
        }
        try {
            Signature.getInstance("SHA-256", providers[0].getName());
            fail("NoSuchAlgorithmException expected");
        } catch (NoSuchAlgorithmException e) {
        }
        Provider[] pp = Security.getProviders();
        for (int i = 0; i < pp.length; i++) {
            try {
                Signature.getInstance("DSA", pp[i].toString());
                fail("NoSuchProviderException expected");
            } catch (NoSuchProviderException e) {
            }
        }
        String[] sp = {null, ""};
        for (int i = 0; i < sp.length; i++) {
            try {
                Signature.getInstance("DSA", sp[i]);
                fail("IllegalArgumentException was not throw for " + sp[i]);
            } catch (IllegalArgumentException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getParameters",
        args = {}
    )
    public void test_getParameters() throws Exception {
        Signature sig = Signature.getInstance("DSA");
        try {
            sig.getParameters();
        } catch (UnsupportedOperationException e) {
        }
        try {
            MySignature sig2 = new MySignature("test");
            sig2.getParameters();
            fail("expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
        try {
            MySignature sig2 = new MySignature("ABC");
            sig2.getParameters();
        } catch (UnsupportedOperationException e) {
            fail("unexpected: " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Just exception case was tested",
        method = "getParameter",
        args = {java.lang.String.class}
    )
    @SuppressWarnings("deprecation")
    public void test_getParameterLjava_lang_String() throws Exception {
        Signature sig = Signature.getInstance("DSA");
        try {
            sig.getParameter("r");
            sig.getParameter("s");
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProvider",
        args = {}
    )
    public void test_getProvider() throws Exception {
        Provider p = Signature.getInstance("DSA").getProvider();
        assertNotNull("provider is null", p);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "initSign",
        args = {java.security.PrivateKey.class}
    )
    public void test_initSignLjava_security_PrivateKey() throws Exception {
        try {
            Signature.getInstance("DSA").initSign(dsaKeys.getPrivate());
        } catch (InvalidKeyException e) {
            fail("unexpected: " + e);
        }
        try { 
            Signature.getInstance("DSA").initSign(rsaKeys.getPrivate());
            fail("expected InvalidKeyException");
        } catch (InvalidKeyException e) {
        }
    }
    @TestTargetNew (
            level=TestLevel.COMPLETE,
            method="initSign",
            args={PrivateKey.class, SecureRandom.class}
    )
    public void test_initSignLjava_security_PrivateKeyLjava_security_SecureRandom() {
        try {
            Signature sig = Signature.getInstance("DSA");
            sig.initSign(dsaKeys.getPrivate(), new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected: " + e);
        } catch (InvalidKeyException e) {
            fail("unexpected: " + e);
        }
        try {
            Signature sig = Signature.getInstance("DSA");
            sig.initSign(rsaKeys.getPrivate(), new SecureRandom());
            fail("expected InvalidKeyException");
        } catch (InvalidKeyException e) {
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected: " + e);
        } 
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "initVerify",
        args = {java.security.PublicKey.class}
    )
    public void test_initVerifyLjava_security_PublicKey() throws Exception {
        Signature.getInstance("DSA").initVerify(dsaKeys.getPublic());
        try {
            Signature.getInstance("DSA").initVerify(rsaKeys.getPublic());
            fail("expected InvalidKeyException");
        } catch (InvalidKeyException e) {
        } 
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "InvalidKeyException checking missed",
        method = "initVerify",
        args = {java.security.cert.Certificate.class}
    )
    public void test_initVerifyLjava_security_Certificate() throws Exception {
        Provider p = new MyProvider();
        p.put("DSA", "tests.java.security.support.cert.MyCertificate$1");
        Provider myProvider = new MyProvider();
        Security.addProvider(myProvider);
        try {
            Provider[] pp = Security.getProviders();
            if (pp == null) {
                return;
            }
            try {
                Signature.getInstance("DSA").initVerify((Certificate) null);
                fail("NullPointerException expected");
            } catch (NullPointerException e) {
            }
        } finally {
            Security.removeProvider(myProvider.getName());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Just exception case was tested",
        method = "setParameter",
        args = {java.lang.String.class, java.lang.Object.class}
    )
    @SuppressWarnings("deprecation")
    public void test_setParameterLjava_lang_StringLjava_lang_Object()
            throws Exception {
        Signature sig = Signature.getInstance("DSA");
        try {
            sig.setParameter("r", BigInteger.ONE);
            sig.setParameter("s", BigInteger.ONE);
        } catch (InvalidParameterException e) {
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Just exception case was tested",
        method = "setParameter",
        args = {java.security.spec.AlgorithmParameterSpec.class}
    )
    public void test_setParameterLjava_security_spec_AlgorithmParameterSpec()
            throws Exception {
        Signature sig = Signature.getInstance("DSA");
        try {
            DSAParameterSpec spec = new DSAParameterSpec(BigInteger.ONE,
                    BigInteger.ONE, BigInteger.ONE);
            sig.setParameter(spec);
        } catch (InvalidParameterException e) {
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Verification of returned value missed. SignatureException checking missed.",
        method = "sign",
        args = {}
    )
    public void test_sign() throws Exception {
        Signature sig = Signature.getInstance("DSA");
        sig.initSign(dsaKeys.getPrivate());
        sig.update(MESSAGE.getBytes());
        sig.sign();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() throws Exception {
        String str = Signature.getInstance("DSA").toString();
        assertNotNull("toString is null", str);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "SignatureException checking missed",
        method = "update",
        args = {byte[].class}
    )
    public void test_update$B() throws Exception {
        Signature sig = Signature.getInstance("DSA");
        sig.initSign(dsaKeys.getPrivate());
        byte[] bytes = MESSAGE.getBytes();
        sig.update(bytes);
        try {
            Signature sig2 = Signature.getInstance("DSA");
            sig2.update(MESSAGE.getBytes());
            fail("expected SignatureException");
        } catch (SignatureException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "SignatureException checking missed. Verification of different values off and len missed.",
        method = "update",
        args = {byte[].class, int.class, int.class}
    )
    public void test_update$BII() throws Exception {
        Signature sig = Signature.getInstance("DSA");
        byte[] bytes = MESSAGE.getBytes();
        try {
            sig.update(bytes, 0, bytes.length);
            fail("expected SignatureException");
        } catch (SignatureException e) {
        }
        sig.initSign(dsaKeys.getPrivate());
        sig.update(bytes, 0, bytes.length);
        sig.update(bytes, bytes.length - 2, 2);
        try {
            sig.update(bytes, bytes.length -3, 4);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            sig.update(null, 0, 5);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "SignatureException checking missed",
        method = "update",
        args = {byte.class}
    )
    public void test_updateB() throws Exception {
        Signature sig = Signature.getInstance("DSA");
        sig.initSign(dsaKeys.getPrivate());
        sig.update(MESSAGE.getBytes()[0]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "update",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_updateLjava_nio_ByteBuffer() throws Exception {
        Signature sig = Signature.getInstance("DSA");
        ByteBuffer buffer = ByteBuffer.allocate(10);
        try {
            sig.update(buffer);
            fail("SignatureException expected");
        } catch (SignatureException e) {
        }
        try {
            sig.initSign(dsaKeys.getPrivate());
            sig.update(buffer);
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "verify",
        args = {byte[].class}
    )
    public void test_verify$B() throws Exception {
        Signature sig = Signature.getInstance("DSA");
        try {
            sig.verify(new byte[] { 0,1,2,3 });
            fail("expected SignatureException");
        } catch (SignatureException e) {
        }
        sig.initSign(dsaKeys.getPrivate());
        sig.update(MESSAGE.getBytes());
        byte[] signature = sig.sign();
        sig.initVerify(dsaKeys.getPublic());
        sig.update(MESSAGE.getBytes());
        assertTrue("Sign/Verify does not pass", sig.verify(signature));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "verify",
        args = {byte[].class, int.class, int.class}
    )
    public void test_verify$BII() throws Exception {
        Signature sig = Signature.getInstance("DSA");
        sig.initSign(dsaKeys.getPrivate());
        sig.update(MESSAGE.getBytes());
        byte[] signature = sig.sign();
        sig.initVerify(dsaKeys.getPublic());
        sig.update(MESSAGE.getBytes());
        assertTrue("Sign/Verify does not pass", sig.verify(signature, 0,
                signature.length));
        try {
            sig.verify(null, 0, signature.length);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            sig.verify(signature, -5, signature.length);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            sig.verify(signature, signature.length, 0);
            fail("SignatureException expected");
        } catch (SignatureException e) {
        }
        try {
            sig.verify(signature, 0, signature.length * 2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    class MyProvider extends Provider {
        private Set<Provider.Service> services = null;
        MyProvider() {
            super("MyProvider", 1.0, "Provider for testing");
            put("MessageDigest.SHA-1", "SomeClassName");
            put("MessageDigest.abc", "SomeClassName");
            put("Alg.Alias.MessageDigest.SHA1", "SHA-1");
            if (services != null) {
                services.clear();
            } else {
                services = new HashSet<Service>();
            }
        }
        MyProvider(String name, double version, String info) {
            super(name, version, info);
            if (services != null) {
                services.clear();
            } else {
                services = new HashSet<Service>();
            }
        }
        public void putService(Provider.Service s) {
            super.putService(s);
            services.add(s);
        }
        public void removeService(Provider.Service s) {
            super.removeService(s);
            services.remove(s);
        }
        public int getNumServices() {
            return services.size();
        }
    }
    @SuppressWarnings("unused")
    private class MySignature extends Signature {
        protected MySignature(String algorithm) {
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
        @Override
        protected AlgorithmParameters engineGetParameters() {
            if (this.getAlgorithm().equals("test")) {
                return super.engineGetParameters();
            } else {
                return null;
            }
        }
    }
}
