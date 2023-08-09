@TestTargetClass(MessageDigest.class)
public class MessageDigest2Test extends junit.framework.TestCase {
    private static final String MESSAGEDIGEST_ID = "MessageDigest.";
    private String[] digestAlgs = null;
    private String providerName = null;
    private static final byte[] AR1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };
    private static final byte[] AR2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };
    private static final String MESSAGE = "abc";
    private static final byte[] MESSAGE_DIGEST = { -87, -103, 62, 54, 71, 6,
            -127, 106, -70, 62, 37, 113, 120, 80, -62, 108, -100, -48, -40,
            -99, };
    private static final byte[] MESSAGE_DIGEST_63_As = { 3, -16, -97, 91, 21,
            -118, 122, -116, -38, -39, 32, -67, -36, 41, -72, 28, 24, -91, 81,
            -11, };
    private static final byte[] MESSAGE_DIGEST_64_As = { 0, -104, -70, -126,
            75, 92, 22, 66, 123, -41, -95, 18, 42, 90, 68, 42, 37, -20, 100,
            77, };
    private static final byte[] MESSAGE_DIGEST_65_As = { 17, 101, 83, 38, -57,
            8, -41, 3, 25, -66, 38, 16, -24, -91, 125, -102, 91, -107, -99, 59, };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "MessageDigest",
        args = {java.lang.String.class}
    )
    public void test_constructor() {
        for (int i = 0; i < digestAlgs.length; i++) {
            MessageDigestStub md = new MessageDigestStub(digestAlgs[i]);
            assertEquals(digestAlgs[i], md.getAlgorithm());
            assertEquals(0, md.getDigestLength());
            assertNull(md.getProvider());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone() {
        for (int i = 0; i < digestAlgs.length; i++) {
            try {
                MessageDigest d1 = MessageDigest.getInstance(digestAlgs[i],
                        providerName);
                for (byte b = 0; b < 84; b++) {
                    d1.update(b);
                }
                MessageDigest d2 = (MessageDigest) d1.clone();
                d1.update((byte) 1);
                d2.update((byte) 1);
                assertTrue("cloned hash differs from original for algorithm "
                        + digestAlgs[i], MessageDigest.isEqual(d1.digest(), d2
                        .digest()));
            } catch (CloneNotSupportedException e) {
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + digestAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            }
        }
    }
    private static final byte[] SHA_DATA_2 = { 70, -54, 124, 120, -29, 57, 56,
            119, -108, -54, -97, -76, -97, -50, -63, -73, 2, 85, -53, -79, };
    private void testSerializationSHA_DATA_2(MessageDigest sha) {
        try {
            sha.reset();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(out);
            output
                    .writeUTF("tests.api.java.security.MessageDigestTest$InitializerFieldsTest3");
            output.writeInt(0); 
            output.writeUTF("java.io.Serializable"); 
            output.writeUTF("sub_toBeNotSerialized"); 
            output.writeInt(9); 
            output.writeUTF("Ljava/lang/String;"); 
            output.writeUTF("sub_toBeNotSerialized2"); 
            output.writeInt(9); 
            output.writeUTF("Ljava/lang/String;"); 
            output.writeUTF("sub_toBeSerialized"); 
            output.writeInt(1); 
            output.writeUTF("Ljava/lang/String;"); 
            output.writeUTF("sub_toBeSerialized3"); 
            output.writeInt(1); 
            output.writeUTF("Ljava/lang/String;"); 
            output.writeUTF("sub_toBeSerialized4"); 
            output.writeInt(1); 
            output.writeUTF("Ljava/lang/String;"); 
            output.writeUTF("sub_toBeSerialized5"); 
            output.writeInt(1); 
            output.writeUTF("Ljava/lang/String;"); 
            output.writeUTF("<clinit>"); 
            output.writeInt(8); 
            output.writeUTF("()V"); 
            output.writeUTF("<init>"); 
            output.writeInt(0); 
            output.writeUTF("()V"); 
            output.writeUTF("equals"); 
            output.writeInt(1); 
            output.writeUTF("(Ljava.lang.Object;)Z"); 
            output.flush();
            byte[] data = out.toByteArray();
            byte[] hash = sha.digest(data);
            assertTrue("SHA_DATA_2 NOT ok", Arrays.equals(hash, SHA_DATA_2));
        } catch (IOException e) {
            fail("SHA_DATA_2 NOT ok");
        }
    }
    private static final byte[] SHA_DATA_1 = { 90, 36, 111, 106, -32, 38, 4,
            126, 21, -51, 107, 45, -64, -68, -109, 112, -31, -46, 34, 115, };
    private void testSerializationSHA_DATA_1(MessageDigest sha) {
        try {
            sha.reset();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(out);
            output
                    .writeUTF("tests.api.java.security.MessageDigestTest$OptionalDataNotRead");
            output.writeInt(0); 
            output.writeUTF("java.io.Serializable"); 
            output.writeUTF("class$0"); 
            output.writeInt(8); 
            output.writeUTF("Ljava/lang/Class;"); 
            output.writeUTF("field1"); 
            output.writeInt(2); 
            output.writeUTF("I"); 
            output.writeUTF("field2"); 
            output.writeInt(2); 
            output.writeUTF("I"); 
            output.writeUTF("<clinit>"); 
            output.writeInt(8); 
            output.writeUTF("()V"); 
            output.writeUTF("<init>"); 
            output.writeInt(1); 
            output.writeUTF("()V"); 
            output.flush();
            byte[] data = out.toByteArray();
            byte[] hash = sha.digest(data);
            assertTrue("SHA_DATA_1 NOT ok", Arrays.equals(hash, SHA_DATA_1));
        } catch (IOException e) {
            fail("SHA_DATA_1 NOT ok");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "digest",
        args = {}
    )
    public void test_digest() {
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA");
            assertNotNull(sha);
        } catch (NoSuchAlgorithmException e) {
            fail("getInstance did not find algorithm");
        }
        sha.update(MESSAGE.getBytes());
        byte[] digest = sha.digest();
        assertTrue("bug in SHA", MessageDigest.isEqual(digest, MESSAGE_DIGEST));
        sha.reset();
        for (int i = 0; i < 63; i++) {
            sha.update((byte) 'a');
        }
        digest = sha.digest();
        assertTrue("bug in SHA", MessageDigest.isEqual(digest,
                MESSAGE_DIGEST_63_As));
        sha.reset();
        for (int i = 0; i < 64; i++) {
            sha.update((byte) 'a');
        }
        digest = sha.digest();
        assertTrue("bug in SHA", MessageDigest.isEqual(digest,
                MESSAGE_DIGEST_64_As));
        sha.reset();
        for (int i = 0; i < 65; i++) {
            sha.update((byte) 'a');
        }
        digest = sha.digest();
        assertTrue("bug in SHA", MessageDigest.isEqual(digest,
                MESSAGE_DIGEST_65_As));
        testSerializationSHA_DATA_1(sha);
        testSerializationSHA_DATA_2(sha);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "digest",
        args = {byte[].class}
    )
    public void test_digest$B() {
        for (int i = 0; i < digestAlgs.length; i++) {
            try {
                MessageDigest digest = MessageDigest.getInstance(digestAlgs[i],
                        providerName);
                assertNotNull(digest);
                digest.digest(AR1);
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + digestAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "digest",
        args = {byte[].class, int.class, int.class}
    )
    public void test_digest$BII() {
        for (int i = 0; i < digestAlgs.length; i++) {
            try {
                MessageDigest digest = MessageDigest.getInstance(digestAlgs[i],
                        providerName);
                assertNotNull(digest);
                int len = digest.getDigestLength();
                byte[] digestBytes = new byte[len];
                digest.digest(digestBytes, 0, digestBytes.length);
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + digestAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            } catch (DigestException e) {
                fail("digest caused exception for algorithm " + digestAlgs[i]
                        + " : " + e);
            }
        }
        try {
            MessageDigest.getInstance("SHA").digest(new byte[] {},
                    Integer.MAX_VALUE, 755);
        } catch (NoSuchAlgorithmException e) {
        } catch (DigestException e) {
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "D",
        method = "update",
        args = {byte[].class, int.class, int.class}
    )
    public void test_update$BII() {
        try {
            MessageDigest.getInstance("SHA").update(new byte[] {},
                    Integer.MAX_VALUE, Integer.MAX_VALUE);
        } catch (NoSuchAlgorithmException e) {
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgorithm",
        args = {}
    )
    public void test_getAlgorithm() {
        for (int i = 0; i < digestAlgs.length; i++) {
            try {
                String alg = MessageDigest.getInstance(digestAlgs[i],
                        providerName).getAlgorithm();
                assertTrue("getAlgorithm ok", alg.equals(digestAlgs[i]));
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + digestAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDigestLength",
        args = {}
    )
    public void test_getDigestLength() {
        for (int i = 0; i < digestAlgs.length; i++) {
            try {
                int len = MessageDigest
                        .getInstance(digestAlgs[i], providerName)
                        .getDigestLength();
                assertTrue("length not ok", len > 0);
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + digestAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_String() {
        for (int i = 0; i < digestAlgs.length; i++) {
            try {
                MessageDigest.getInstance(digestAlgs[i]);
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + digestAlgs[i]);
            }
        }
        try {
            MessageDigest.getInstance("UnknownDigest");
            fail("expected NoSuchAlgorithmException");
        } catch (NoSuchAlgorithmException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_lang_String() {
        for (int i = 0; i < digestAlgs.length; i++) {
            try {
                MessageDigest.getInstance(digestAlgs[i], providerName);
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + digestAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            }
        }
        try {
            MessageDigest.getInstance(digestAlgs[0], "UnknownProvider");
            fail("expected NoSuchProviderException");
        } catch (NoSuchProviderException e) {
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        }
        try {
            MessageDigest.getInstance("UnknownDigest", providerName);
            fail("expected NoSuchAlgorithmException");
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchProviderException e) {
            fail("unexpected exception: " + e);
        }
        try {
            MessageDigest.getInstance(null, providerName);
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        } catch (NoSuchProviderException e) {
            fail("unexpected exception: " + e);
        }
        try {
            MessageDigest.getInstance("AnyDigest", (String)null);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        } catch (NoSuchProviderException e) {
            fail("unexpected exception: " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_security_Provider() {
        Provider[] providers = Security.getProviders("MessageDigest.SHA");
        for (int i = 0; i < digestAlgs.length; i++) {
            for (int j = 0; j < providers.length; j++) {
                try {
                    MessageDigest.getInstance(digestAlgs[i], providers[j]);
                } catch (NoSuchAlgorithmException e) {
                    fail("getInstance did not find algorithm " + digestAlgs[i]);
                }
            }
        }
        try {
            MessageDigest.getInstance(null, new TestProvider());
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        }
        try {
            MessageDigest.getInstance("UnknownDigest", new TestProvider());
            fail("expected NoSuchAlgorithmException");
        } catch (NoSuchAlgorithmException e) {
        }
        try {
            MessageDigest.getInstance("AnyDigest", (Provider)null);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProvider",
        args = {}
    )
    public void test_getProvider() {
        for (int i = 0; i < digestAlgs.length; i++) {
            try {
                Provider p = MessageDigest.getInstance(digestAlgs[i],
                        providerName).getProvider();
                assertNotNull("provider is null", p);
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + digestAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Otherwise case is not checked",
        method = "isEqual",
        args = {byte[].class, byte[].class}
    )
    public void test_isEqual$B$B() {
        assertTrue("isEqual is not correct", MessageDigest.isEqual(AR1, AR2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        try {
            String str = MessageDigest.getInstance("SHA").toString();
            assertNotNull("toString is null", str);
        } catch (NoSuchAlgorithmException e) {
            fail("getInstance did not find algorithm");
        }
    }
    protected void setUp() {
        if (digestAlgs == null) {
            Provider[] providers = Security.getProviders("MessageDigest.SHA");
            if (providers == null) {
                fail("No providers available for test");
            }
            providerName = providers[0].getName();
            digestAlgs = getDigestAlgorithms(providerName);
            if (digestAlgs == null || digestAlgs.length == 0) {
                fail("No digest algorithms were found");
            }
        }
    }
    private String[] getDigestAlgorithms(String providerName) {
        Vector<String> algs = new Vector<String>();
        Provider provider = Security.getProvider(providerName);
        if (provider == null)
            return new String[0];
        Enumeration e = provider.keys();
        while (e.hasMoreElements()) {
            String algorithm = (String) e.nextElement();
            if (algorithm.startsWith(MESSAGEDIGEST_ID)
                    && !algorithm.contains(" ")) {
                algs.addElement(algorithm.substring(MESSAGEDIGEST_ID.length()));
            }
        }
        return (String[]) algs.toArray(new String[algs.size()]);
    }
    private class MessageDigestStub extends MessageDigest {
        public MessageDigestStub(String algorithm) {
            super(algorithm);
        }
        public byte[] engineDigest() {
            return null;
        }
        public void engineReset() {
        }
        public void engineUpdate(byte input) {
        }
        public void engineUpdate(byte[] input, int offset, int len) {
        }
    }
    private static class TestProvider extends Provider {
        protected TestProvider() {
            super("TestProvider", 1.0, "INFO");
        }
    }
}
