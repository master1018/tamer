@TestTargetClass(MessageDigest.class)
public class MessageDigest1Test extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "reset",
        args = {}
    )
    public void test_reset() {
        MyMessageDigest1 md = new MyMessageDigest1("ABC");
        md.reset();
        assertTrue(md.runEngineReset);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "update",
        args = {byte.class}
    )
    public void test_updateLB() {
        MyMessageDigest1 md = new MyMessageDigest1("ABC");
        md.update((byte) 1);
        assertTrue(md.runEngineUpdate1);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "update",
        args = {byte[].class, int.class, int.class}
    )
    public void test_updateLB$LILI() {
        MyMessageDigest1 md = new MyMessageDigest1("ABC");
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        md.update(bytes, 1, 2);
        assertTrue(md.runEngineUpdate2);
        try {
            md.update(null, 0, 1);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            md.update(bytes, 0, bytes.length + 1);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            md.update(bytes, Integer.MAX_VALUE, 1);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        final int offset = -1;
        final int len = -1;
        md = new MyMessageDigest1("ABC") {
            @Override
            public void engineUpdate(byte[] arg0, int arg1, int arg2) {
                assertSame("buf", bytes, arg0);
                assertEquals("offset", offset, arg1);
                assertEquals("len", len, arg2);
                runEngineUpdate2 = true;
            }
        };
        md.update(bytes, offset, len);
        assertTrue(md.runEngineUpdate2);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "update",
        args = {byte[].class}
    )
    public void test_updateLB$() {
        MyMessageDigest1 md = new MyMessageDigest1("ABC");
        byte[] b = { 1, 2, 3, 4, 5 };
        md.update(b);
        assertTrue(md.runEngineUpdate2);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "update",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_updateLjava_nio_ByteBuffer() {
        MyMessageDigest1 md = new MyMessageDigest1("ABC");
        byte[] b = { 1, 2, 3, 4, 5 };
        ByteBuffer byteBuffer = ByteBuffer.wrap(b);
        int limit = byteBuffer.limit();
        md.update(byteBuffer);
        assertTrue(md.runEngineUpdate2);
        assertEquals(byteBuffer.limit(), byteBuffer.position());
        assertEquals(limit, byteBuffer.limit());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "digest",
        args = {}
    )
    public void test_digest() {
        MyMessageDigest1 md = new MyMessageDigest1("ABC");
        assertEquals("incorrect result", 0, md.digest().length);
        assertTrue(md.runEngineDigest);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "digest",
        args = {byte[].class}
    )
    public void test_digestLB$() {
        MyMessageDigest1 md = new MyMessageDigest1("ABC");
        byte[] b = { 1, 2, 3, 4, 5 };
        assertEquals("incorrect result", 0, md.digest(b).length);
        assertTrue(md.runEngineDigest);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "digest",
        args = {byte[].class, int.class, int.class}
    )
    public void test_digestLB$LILI() throws Exception {
        MyMessageDigest1 md = new MyMessageDigest1("ABC");
        byte[] b = { 1, 2, 3, 4, 5 };
        assertEquals("incorrect result", 0, md.digest(b, 2, 3));
        assertTrue("digest failed", md.runEngineDigest);
        md = new MyMessageDigest1();
        final byte[] bytes = new byte[] { 2, 4, 1 };
        try {
            md.digest(null, 0, 1);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            md.digest(bytes, 0, bytes.length + 1);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            md.digest(bytes, Integer.MAX_VALUE, 1);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        final int offset = -1;
        final int len = -1;
        final int status = 33;
        md = new MyMessageDigest1("ABC") {
            @Override
            public int engineDigest(byte[] arg0, int arg1, int arg2) {
                assertSame("buf", bytes, arg0);
                assertEquals("offset", offset, arg1);
                assertEquals("len", len, arg2);
                return status;
            }
        };
        assertEquals("returned status", status, md.digest(bytes, offset, len));
        try {
            MessageDigest digest = MessageDigest.getInstance("TestDigest", new TestProvider());
            digest.digest(new byte[5], 0, 5);
            fail("expected DigestException");
        } catch (DigestException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isEqual",
        args = {byte[].class, byte[].class}
    )
    public void test_isEqualLB$LB$() {
        byte[] b1 = { 1, 2, 3, 4 };
        byte[] b2 = { 1, 2, 3, 4, 5 };
        byte[] b3 = { 1, 3, 3, 4 };
        byte[] b4 = { 1, 2, 3, 4 };
        assertTrue(MessageDigest.isEqual(b1, b4));
        assertFalse(MessageDigest.isEqual(b1, b2));
        assertFalse(MessageDigest.isEqual(b1, b3));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgorithm",
        args = {}
    )
    public void test_getAlgorithm() {
        MyMessageDigest1 md = new MyMessageDigest1("ABC");
        assertEquals("ABC", md.getAlgorithm());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProvider",
        args = {}
    )
    public void test_getProvider() {
        MyMessageDigest1 md = new MyMessageDigest1("ABC");
        assertNull(md.getProvider());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDigestLength",
        args = {}
    )
    public void test_getDigestLength() {
        MyMessageDigest1 md = new MyMessageDigest1("ABC");
        assertEquals(0, md.getDigestLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testSHAProvider() {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        }
        byte[] bytes = new byte[] { 1, 1, 1, 1, 1 };
        try {
            md.update(bytes, -1, 1);
            fail("No expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        md.update(bytes, 1, -1);
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        }
        try {
            md.digest(bytes, 0, -1);
            fail("No expected DigestException");
        } catch (DigestException e) {
        }
        try {
            md.digest(bytes, -1, 0);
            fail("No expected DigestException");
        } catch (DigestException e) {
        }
        try {
            md = MessageDigest.getInstance("UnknownDigest");
            fail("expected NoSuchAlgorithmException");
        } catch (NoSuchAlgorithmException e) {
        }
    }
    class TestProvider extends Provider {
        public TestProvider() {
            super("TestProvider", 1.0, "info");
            put("MessageDigest.TestDigest", TestMessageDigestSpi.class.getName());
        }
    }
    public static class TestMessageDigestSpi extends MessageDigestSpi {
        public TestMessageDigestSpi() {
        }
        @Override
        protected byte[] engineDigest() {
            return new byte[]{3,4,5,6,7,8,9,3,45,6,7,}; 
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
        @Override
        protected int engineGetDigestLength() {
            return 42;
        }
    }
}
