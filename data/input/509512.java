@TestTargetClass(MessageDigestSpi.class)
public class MessageDigestSpiTest extends TestCase {
    @SuppressWarnings("cast")
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "MessageDigestSpi",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineDigest",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineReset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineUpdate",
            args = {byte.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineUpdate",
            args = {byte[].class, int.class, int.class}
        )
    })
   public void test_constructor() {
        MyMessageDigest mds;
        try {
            mds = new MyMessageDigest();
            assertTrue(mds instanceof MessageDigestSpi);
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
        try {
            mds = new MyMessageDigest();
            byte[] ba = {0, 1, 2, 3, 4, 5};
            mds.engineDigest();
            mds.engineReset();
            mds.engineUpdate(ba[0]);
            mds.engineUpdate(ba, 0, ba.length);
        } catch (Exception e) {
            fail("Unexpected exception for abstract methods");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "engineDigest",
        args = {byte[].class, int.class, int.class}
    )
    public void test_engineDigestLB$LILI() throws Exception {
        final int DIGEST_LENGTH = 2;
        MyMessageDigest md = new MyMessageDigest() {
            public int engineGetDigestLength() {
                return DIGEST_LENGTH;
            }
            public byte[] engineDigest() {
                return new byte[DIGEST_LENGTH]; 
            }
        };
        byte[] b = new byte[5];
        try {
            md.engineDigest(null, 1, DIGEST_LENGTH);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            md.engineDigest(b, 1, DIGEST_LENGTH - 1);
            fail("No expected DigestException");
        } catch (DigestException e) {
        }
        try {
            md.engineDigest(b, b.length + 1, b.length);
            fail("No expected DigestException - 1");
        } catch (DigestException e) {
        }
        try {
            md.engineDigest(b, -1, b.length);
            fail("No expected DigestException");
        } catch (ArrayIndexOutOfBoundsException e) {
        } catch (DigestException e) {
        }
        assertEquals("incorrect result", DIGEST_LENGTH, md
                .engineDigest(b, 1, 3));
        md = new MyMessageDigest();
        try {
            md.engineDigest(b, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "engineGetDigestLength",
        args = {}
    )
    public void test_engineGetDigestLength() {
        MyMessageDigest md = new MyMessageDigest();
        assertEquals(0, md.engineGetDigestLength());
        MessageDigest md5Digest = null;
        try {
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected Exception: " + e);
        }
        assertEquals(16, md5Digest.getDigestLength());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "engineUpdate",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_engineUpdateLjava_nio_ByteBuffer() {
        MyMessageDigest md = new MyMessageDigest();
        byte[] b = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        ByteBuffer buf = ByteBuffer.wrap(b, 0, b.length);
        buf.get(b);
        int limit = buf.limit();
        md.engineUpdate(buf);
        assertEquals(limit, buf.limit());
        assertEquals(limit, buf.position());
        buf = ByteBuffer.wrap(b, 0, b.length);
        buf.get();
        buf.get();
        buf.get();
        md.engineUpdate(buf);
        assertEquals(limit, buf.limit());
        assertEquals(limit, buf.position());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone() throws CloneNotSupportedException {
        MyMessageDigest md = new MyMessageDigest();
        try {
            md.clone();
            fail("No expected CloneNotSupportedException");
        } catch (CloneNotSupportedException e) {
        }
        MyMessageDigestCloneable mdc = new MyMessageDigestCloneable();
        assertNotSame(mdc, mdc.clone());
    }
    private class MyMessageDigest extends MessageDigestSpi {
        @Override
        public void engineReset() {
        }
        @Override
        public byte[] engineDigest() {
            return null;
        }
        @Override
        public void engineUpdate(byte arg0) {
        }
        @Override
        public void engineUpdate(byte[] arg0, int arg1, int arg2) {
        }
        @Override
        protected int engineDigest(byte[] buf, int offset, int len)
                throws DigestException {
            return super.engineDigest(buf, offset, len);
        }
        @Override
        protected int engineGetDigestLength() {
            return super.engineGetDigestLength();
        }
        @Override
        protected void engineUpdate(ByteBuffer input) {
            super.engineUpdate(input);
        }
    }
    private class MyMessageDigestCloneable extends MyMessageDigest implements
            Cloneable {
    }
}
