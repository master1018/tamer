@TestTargetClass(DigestInputStream.class)
public class DigestInputStreamTest extends TestCase {
    private static final String algorithmName[] = {
            "SHA-1",
            "SHA",
            "SHA1",
            "SHA-256",
            "SHA-384",
            "SHA-512",
            "MD5",
    };
    private static final int CHUNK_SIZE = 32;
    private static final byte[] myMessage = MDGoldenData.getMessage();
    private static final int MY_MESSAGE_LEN = myMessage.length;
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies both non null parameters",
        method = "DigestInputStream",
        args = {java.io.InputStream.class, java.security.MessageDigest.class}
    )
    public final void testDigestInputStream01()  {
        for (int i=0; i<algorithmName.length; i++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[i]);
                InputStream is = new ByteArrayInputStream(myMessage);
                InputStream dis = new DigestInputStream(is, md);
                assertTrue(dis instanceof DigestInputStream);
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies both null parameters. Need cases where just one parameter null",
        method = "DigestInputStream",
        args = {java.io.InputStream.class, java.security.MessageDigest.class}
    )
    public final void testDigestInputStream02() {
        InputStream dis = new DigestInputStream(null, null);
        assertTrue(dis instanceof DigestInputStream);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {}
    )
    public final void testRead01()
        throws IOException {
        for (int ii=0; ii<algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                for (int i=0; i<MY_MESSAGE_LEN; i++) {
                    assertTrue("retval", ((byte)dis.read() == myMessage[i]));
                }
                assertTrue("update",
                        Arrays.equals(
                                dis.getMessageDigest().digest(),
                                MDGoldenData.getDigest(algorithmName[ii])));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {}
    )
    public final void testRead02()
        throws IOException {
        for (int ii=0; ii<algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                for (int i=0; i<MY_MESSAGE_LEN; i++) {
                    dis.read();
                }
                assertEquals("retval1", -1, dis.read());
                assertEquals("retval2", -1, dis.read());
                assertEquals("retval3", -1, dis.read());
                assertTrue("update",
                        Arrays.equals(dis.getMessageDigest().digest(),
                                MDGoldenData.getDigest(algorithmName[ii])));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {}
    )
    public final void testRead03()
        throws IOException {
        for (int ii=0; ii<algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                dis.on(false);
                for (int i=0; i<MY_MESSAGE_LEN; i++) {
                    dis.read();
                }
                assertTrue(Arrays.equals(dis.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[ii]+"_NU")));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IOException is not tested",
        method = "read",
        args = {}
    )
    public final void testRead04() throws IOException {
        for (int ii=0; ii<algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                DigestInputStream dis = new DigestInputStream(null, md);
                try {
                    for (int i=0; i<MY_MESSAGE_LEN; i++) {
                        dis.read();
                    }
                } catch (Exception e) {
                    return;
                }
                fail("InputStream not set. read() must not work");
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IOException is not tested",
        method = "read",
        args = {}
    )
    public final void testRead05() {
        InputStream is = new ByteArrayInputStream(myMessage);
        DigestInputStream dis = new DigestInputStream(is, null);
        try {
            for (int i=0; i<MY_MESSAGE_LEN; i++) {
                dis.read();
            }
            fail("read() must not work when digest functionality is on");
        } catch (Exception e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {}
    )
    public final void testRead06()
        throws IOException {
        InputStream is = new ByteArrayInputStream(myMessage);
        DigestInputStream dis = new DigestInputStream(is, null);
        dis.on(false);
        for (int i=0; i<MY_MESSAGE_LEN; i++) {
            assertTrue((byte)dis.read() == myMessage[i]);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "read",
        args = {byte[].class, int.class, int.class}
    )
    public final void testReadbyteArrayintint01()
        throws IOException {        
        for (int ii=0; ii<algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                byte[] bArray = new byte[MY_MESSAGE_LEN];
                assertTrue("retval",
                        dis.read(bArray, 0, bArray.length) == MY_MESSAGE_LEN);
                assertTrue("bArray", Arrays.equals(myMessage, bArray));
                assertTrue("update", Arrays.equals(dis.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[ii])));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "read",
        args = {byte[].class, int.class, int.class}
    )
    public final void testReadbyteArrayintint02()
        throws IOException {
        assertEquals(0, MY_MESSAGE_LEN % CHUNK_SIZE);
        for (int ii=0; ii<algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                byte[] bArray = new byte[MY_MESSAGE_LEN];
                for (int i=0; i<MY_MESSAGE_LEN/CHUNK_SIZE; i++) {
                    assertTrue("retval",
                            dis.read(bArray, i*CHUNK_SIZE, CHUNK_SIZE) == CHUNK_SIZE);
                }
                assertTrue("bArray", Arrays.equals(myMessage, bArray));
                assertTrue("update", Arrays.equals(dis.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[ii])));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "read",
        args = {byte[].class, int.class, int.class}
    )
    public final void testReadbyteArrayintint03()
        throws IOException {
        assertTrue(MY_MESSAGE_LEN % (CHUNK_SIZE+1) != 0);
        for (int ii=0; ii<algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                byte[] bArray = new byte[MY_MESSAGE_LEN];
                for (int i=0; i<MY_MESSAGE_LEN/(CHUNK_SIZE+1); i++) {
                    assertTrue("retval1",
                            dis.read(bArray, i*(CHUNK_SIZE+1), CHUNK_SIZE+1) ==
                                CHUNK_SIZE + 1);
                }
                assertTrue("retval2",
                        dis.read(bArray,
                                MY_MESSAGE_LEN/(CHUNK_SIZE+1)*(CHUNK_SIZE+1),
                                MY_MESSAGE_LEN % (CHUNK_SIZE+1)) ==
                                    (MY_MESSAGE_LEN % (CHUNK_SIZE+1)));
                assertTrue("bArray", Arrays.equals(myMessage, bArray));
                assertTrue("update", Arrays.equals(dis.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[ii])));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "read",
        args = {byte[].class, int.class, int.class}
    )
    public final void testReadbyteArrayintint04()
        throws IOException {        
        for (int ii=0; ii<algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                byte[] bArray = new byte[MY_MESSAGE_LEN];
                dis.read(bArray, 0, bArray.length);
                assertEquals("retval1", -1, dis.read(bArray, 0, 1));
                assertEquals("retval2", -1, dis.read(bArray, 0, bArray.length));
                assertEquals("retval3", -1, dis.read(bArray, 0, 1));
                assertTrue("update",
                        Arrays.equals(dis.getMessageDigest().digest(),
                                MDGoldenData.getDigest(algorithmName[ii])));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "read",
        args = {byte[].class, int.class, int.class}
    )
    public final void testReadbyteArrayintint05()
        throws IOException {
        assertEquals(0, MY_MESSAGE_LEN % CHUNK_SIZE);
        for (int ii=0; ii<algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                byte[] bArray = new byte[MY_MESSAGE_LEN];
                dis.on(false);
                for (int i=0; i<MY_MESSAGE_LEN/CHUNK_SIZE; i++) {
                    dis.read(bArray, i*CHUNK_SIZE, CHUNK_SIZE);
                }
                assertTrue(Arrays.equals(dis.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[ii]+"_NU")));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessageDigest",
        args = {}
    )
    public final void testGetMessageDigest() {
        for (int ii=0; ii<algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                DigestInputStream dis = new DigestInputStream(null, md);
                assertTrue(dis.getMessageDigest() == md);
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Method setMessageDigest is not tested with null parameter",
        method = "setMessageDigest",
        args = {java.security.MessageDigest.class}
    )
    public final void testSetMessageDigest() {
        for (int ii=0; ii<algorithmName.length; ii++) {
            try {
                DigestInputStream dis = new DigestInputStream(null, null);
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                dis.setMessageDigest(md);
                assertTrue(dis.getMessageDigest() == md);
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "on",
        args = {boolean.class}
    )
    public final void testOn() throws IOException {
        for (int ii=0; ii<algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                dis.on(false);
                for (int i=0; i<MY_MESSAGE_LEN-1; i++) {
                    dis.read();
                }
                dis.on(true);
                dis.read();
                byte[] digest = dis.getMessageDigest().digest();
                assertFalse(
                        Arrays.equals(digest,MDGoldenData.getDigest(algorithmName[ii])) ||
                        Arrays.equals(digest,MDGoldenData.getDigest(algorithmName[ii]+"_NU")));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public final void testToString() {
        for (int ii=0; ii<algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                assertNotNull(dis.toString());
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
}
