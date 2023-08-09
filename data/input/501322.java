@TestTargetClass(DigestOutputStream.class)
public class DigestOutputStreamTest extends TestCase {
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
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DigestOutputStream",
        args = {java.io.OutputStream.class, java.security.MessageDigest.class}
    )
    public void test_CtorLjava_io_OutputStreamLjava_security_MessageDigest() {
        MessageDigest md = new MyMessageDigest1();
        MyOutputStream out = new MyOutputStream();
        MyDigestOutputStream dos = new MyDigestOutputStream(out, md);
        assertSame(out, dos.myOutputStream());
        assertSame(md, dos.myMessageDigest());
        dos = new MyDigestOutputStream(null, null);
        assertNull(dos.myOutputStream());
        assertNull(dos.myMessageDigest());
        dos = new MyDigestOutputStream(null, md);
        assertNull(dos.myOutputStream());
        assertNotNull(dos.myMessageDigest());
        dos = new MyDigestOutputStream(out, null);
        assertNotNull(dos.myOutputStream());
        assertNull(dos.myMessageDigest());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessageDigest",
        args = {}
    )
    public void test_getMessageDigest() {
        MessageDigest digest = new MyMessageDigest1();
        OutputStream out = new MyOutputStream();
        DigestOutputStream dos = new DigestOutputStream(out, digest);
        assertSame(digest, dos.getMessageDigest());
        dos = new DigestOutputStream(out, null);
        assertNull("getMessageDigest should have returned null", dos
                .getMessageDigest());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setMessageDigest",
        args = {java.security.MessageDigest.class}
    )
    public void test_setMessageDigestLjava_security_MessageDigest() {
        MessageDigest digest = new MyMessageDigest1();
        OutputStream out = new MyOutputStream();
        DigestOutputStream dos = new DigestOutputStream(out, null);
        dos.setMessageDigest(digest);
        assertSame(digest, dos.getMessageDigest());
        dos.setMessageDigest(null);
        assertNull("getMessageDigest should have returned null", dos
                .getMessageDigest());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {int.class}
    )
    public final void testWriteint01()
        throws IOException {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                DigestOutputStream dos = new DigestOutputStream(bos, md);
                for (int i=0; i<MY_MESSAGE_LEN; i++) {
                    dos.write(myMessage[i]);
                }
                assertTrue("write", Arrays.equals(MDGoldenData.getMessage(),
                        bos.toByteArray()));
                assertTrue("update", Arrays.equals(dos.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[k])));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {int.class}
    )
    public final void testWriteint02()
        throws IOException {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                DigestOutputStream dos = new DigestOutputStream(bos, md);
                dos.on(false);
                for (int i=0; i<MY_MESSAGE_LEN; i++) {
                    dos.write(myMessage[i]);
                }
                assertTrue("write", Arrays.equals(MDGoldenData.getMessage(),
                        bos.toByteArray()));
                assertTrue("update", Arrays.equals(dos.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[k]+"_NU")));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IOException isn't tested",
        method = "write",
        args = {int.class}
    )
    public final void testWriteint03() throws IOException {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(null, md);
                try {
                    for (int i=0; i<MY_MESSAGE_LEN; i++) {
                        dos.write(myMessage[i]);
                    }
                    fail("OutputStream not set. write(int) must not work");
                } catch (Exception e) {
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IOException isn't tested",
        method = "write",
        args = {int.class}
    )
    public final void testWriteint04() throws IOException {
        OutputStream os = new ByteArrayOutputStream(MY_MESSAGE_LEN);
        DigestOutputStream dos = new DigestOutputStream(os, null);
        try {
            for (int i=0; i<MY_MESSAGE_LEN; i++) {
                dos.write(myMessage[i]);
            }
            fail("OutputStream not set. write(int) must not work");
        } catch (Exception e) {
            return;
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {int.class}
    )
    public final void testWriteint05() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
        DigestOutputStream dos = new DigestOutputStream(bos, null);
        dos.on(false);
        for (int i=0; i<MY_MESSAGE_LEN; i++) {
            dos.write(myMessage[i]);
        }
        assertTrue(Arrays.equals(MDGoldenData.getMessage(),
                bos.toByteArray()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "write",
        args = {byte[].class, int.class, int.class}
    )
    public final void test_write$BII_1() throws IOException {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(bos, md);
                dos.write(myMessage, 0, MY_MESSAGE_LEN);
                assertTrue("write", Arrays.equals(myMessage, bos.toByteArray()));
                assertTrue("update", Arrays.equals(dos.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[k])));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "write",
        args = {byte[].class, int.class, int.class}
    )
    public final void test_write$BII_2() throws IOException {
        assertEquals(0, MY_MESSAGE_LEN % CHUNK_SIZE);
        for (int k=0; k<algorithmName.length; k++) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(bos, md);
                for (int i=0; i<MY_MESSAGE_LEN/CHUNK_SIZE; i++) {
                    dos.write(myMessage, i*CHUNK_SIZE, CHUNK_SIZE);
                }
                assertTrue("write", Arrays.equals(myMessage, bos.toByteArray()));
                assertTrue("update", Arrays.equals(dos.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[k])));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "write",
        args = {byte[].class, int.class, int.class}
    )
    public final void test_write$BII_3()
        throws NoSuchAlgorithmException,
               IOException {
        assertTrue(MY_MESSAGE_LEN % (CHUNK_SIZE+1) != 0);
        for (int k=0; k<algorithmName.length; k++) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(bos, md);
                for (int i=0; i<MY_MESSAGE_LEN/(CHUNK_SIZE+1); i++) {
                    dos.write(myMessage, i*(CHUNK_SIZE+1), CHUNK_SIZE+1);
                }
                dos.write(myMessage,
                        MY_MESSAGE_LEN/(CHUNK_SIZE+1)*(CHUNK_SIZE+1),
                        MY_MESSAGE_LEN % (CHUNK_SIZE+1));
                assertTrue("write", Arrays.equals(myMessage, bos.toByteArray()));
                assertTrue("update", Arrays.equals(dos.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[k])));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "write",
        args = {byte[].class, int.class, int.class}
    )
    public final void test_write$BII_4()
        throws NoSuchAlgorithmException,
               IOException {
        assertEquals(0, MY_MESSAGE_LEN % CHUNK_SIZE);
        for (int k=0; k<algorithmName.length; k++) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(bos, md);
                dos.on(false);
                for (int i=0; i<MY_MESSAGE_LEN/CHUNK_SIZE; i++) {
                    dos.write(myMessage, i*CHUNK_SIZE, CHUNK_SIZE);
                }
                assertTrue("write", Arrays.equals(myMessage, bos.toByteArray()));
                assertTrue("update", Arrays.equals(dos.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[k]+"_NU")));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Illegal argument checks.",
        method = "write",
        args = {byte[].class, int.class, int.class}
    )
    public void test_write$BII_6() throws Exception {
        MessageDigest md = new MyMessageDigest1();
        byte[] bytes = new byte[] { 1, 2 };
        DigestOutputStream dig = new DigestOutputStream(
                new ByteArrayOutputStream(), md);
        try {
            dig.write(null, -1, 0);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            dig.write(bytes, 0, bytes.length + 1);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            dig.write(bytes, -1, 1);
            fail("No expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            dig.write(bytes, 0, -1);
            fail("No expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IOException check.",
        method = "write",
        args = {byte[].class, int.class, int.class}
    )    
    public void test_write$BII_7() 
        throws IOException, NoSuchAlgorithmException {
        Support_OutputStream sos = new Support_OutputStream(MY_MESSAGE_LEN);
        MessageDigest md = MessageDigest.getInstance(algorithmName[0]);
        DigestOutputStream dos = new DigestOutputStream(sos, md);
        dos.write(myMessage, 0, MY_MESSAGE_LEN);
        try {
            dos.write(myMessage, 0, MY_MESSAGE_LEN);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "on",
        args = {boolean.class}
    )
    public final void testOn() throws IOException {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(bos, md);
                dos.on(false);
                for (int i=0; i<MY_MESSAGE_LEN-1; i++) {
                    dos.write(myMessage[i]);
                }
                dos.on(true);
                dos.write(myMessage[MY_MESSAGE_LEN-1]);
                byte[] digest = dos.getMessageDigest().digest();
                assertFalse(
                        Arrays.equals(digest,MDGoldenData.getDigest(algorithmName[k])) ||
                        Arrays.equals(digest,MDGoldenData.getDigest(algorithmName[k]+"_NU")));
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
    public final void testToString() throws NoSuchAlgorithmException {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(bos, md);
                assertNotNull(dos.toString());
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
    public void test_onZ() throws Exception {
        DigestOutputStream dos = new DigestOutputStream(
                new ByteArrayOutputStream(), MessageDigest
                        .getInstance("SHA"));
        dos.on(false);
        byte digestArray[] = { 23, 43, 44 };
        dos.write(digestArray, 1, 1);
        byte digestResult[] = dos.getMessageDigest().digest();
        byte expected[] = { -38, 57, -93, -18, 94, 107, 75, 13, 50, 85,
                -65, -17, -107, 96, 24, -112, -81, -40, 7, 9 };
        assertTrue("Digest did not return expected result.",
                java.util.Arrays.equals(digestResult, expected));
        dos.on(true);
        dos.write(digestArray, 1, 1);
        digestResult = dos.getMessageDigest().digest();
        byte expected1[] = { -87, 121, -17, 16, -52, 111, 106, 54, -33,
                107, -118, 50, 51, 7, -18, 59, -78, -30, -37, -100 };
        assertTrue("Digest did not return expected result.",
                java.util.Arrays.equals(digestResult, expected1));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "write",
        args = {byte[].class, int.class, int.class}
    )
    public void test_write$BII_5() throws Exception {
            DigestOutputStream dos = new DigestOutputStream(
                new ByteArrayOutputStream(), MessageDigest.getInstance("SHA"));
            byte digestArray[] = { 23, 43, 44 };
            dos.write(digestArray, 1, 1);
            byte digestResult[] = dos.getMessageDigest().digest();
            byte expected[] = { -87, 121, -17, 16, -52, 111, 106, 54, -33, 107,
                    -118, 50, 51, 7, -18, 59, -78, -30, -37, -100 };
            assertTrue("Digest did not return expected result.",
                    java.util.Arrays.equals(digestResult, expected));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {int.class}
    )
    public void test_writeI() throws Exception {
            DigestOutputStream dos = new DigestOutputStream(
                new ByteArrayOutputStream(), MessageDigest.getInstance("SHA"));
            dos.write((byte) 43);
            byte digestResult[] = dos.getMessageDigest().digest();
            byte expected[] = { -87, 121, -17, 16, -52, 111, 106, 54, -33, 107,
                    -118, 50, 51, 7, -18, 59, -78, -30, -37, -100 };
            assertTrue("Digest did not return expected result.",
                    java.util.Arrays.equals(digestResult, expected));
    }
    private class MyOutputStream extends OutputStream {
        @Override
        public void write(int arg0) throws IOException {
        }
    }
    private class MyDigestOutputStream extends DigestOutputStream {
        public MyDigestOutputStream(OutputStream out, MessageDigest digest) {
            super(out, digest);
        }
        public MessageDigest myMessageDigest() {
            return digest;
        }
        public OutputStream myOutputStream() {
            return out;
        }
    }
}
