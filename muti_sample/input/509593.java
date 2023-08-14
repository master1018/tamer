@TestTargetClass(DigestInputStream.class)
public class DigestInputStream2Test extends junit.framework.TestCase {
    ByteArrayInputStream inStream;
    ByteArrayInputStream inStream1;
    MessageDigest digest;
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies case with non null parameters only",
        method = "DigestInputStream",
        args = {java.io.InputStream.class, java.security.MessageDigest.class}
    )
    public void test_ConstructorLjava_io_InputStreamLjava_security_MessageDigest() {
        DigestInputStream dis = new DigestInputStream(inStream, digest);
        assertNotNull("Constructor returned null instance", dis);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessageDigest",
        args = {}
    )
    public void test_getMessageDigest() {
        DigestInputStream dis = new DigestInputStream(inStream, digest);
        assertEquals("getMessageDigest returned a bogus result", digest, dis
                .getMessageDigest());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "on",
        args = {boolean.class}
    )
    public void test_onZ() throws Exception {
        MessageDigest originalDigest = (MessageDigest) (digest.clone());
        MessageDigest noChangeDigest = (MessageDigest) (digest.clone());
        DigestInputStream dis = new DigestInputStream(inStream, noChangeDigest);
        dis.on(false);
        int c = dis.read();
        assertEquals('T', c);
        assertTrue("MessageDigest changed even though processing was off",
                MessageDigest.isEqual(noChangeDigest.digest(), originalDigest
                        .digest()));
        MessageDigest changeDigest = (MessageDigest) (digest.clone());
        dis = new DigestInputStream(inStream, digest);
        dis.on(true);
        c = dis.read();
        assertEquals('h', c);
        assertTrue("MessageDigest did not change with processing on",
                !MessageDigest.isEqual(digest.digest(), changeDigest.digest()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies just one positive case for method read()",
        method = "read",
        args = {}
    )
    public void test_read() throws IOException {
        DigestInputStream dis = new DigestInputStream(inStream, digest);
        int c;
        while ((c = dis.read()) > -1) {
            int d = inStream1.read();
            assertEquals(d, c);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies one positive case for method read(byte[], int, int)",
        method = "read",
        args = {byte[].class, int.class, int.class}
    )
    public void test_read$BII() throws IOException {
        DigestInputStream dis = new DigestInputStream(inStream, digest);
        int bytesToRead = inStream.available();
        byte buf1[] = new byte[bytesToRead + 5];
        byte buf2[] = new byte[bytesToRead + 5];
        assertTrue("No data to read for this test", bytesToRead>0);
        int bytesRead1 = dis.read(buf1, 5, bytesToRead);
        int bytesRead2 = inStream1.read(buf2, 5, bytesToRead);
        assertEquals("Didn't read the same from each stream", bytesRead1,
                bytesRead2);
        assertEquals("Didn't read the entire", bytesRead1, bytesToRead);
        boolean same = true;
        for (int i = 0; i < bytesToRead + 5; i++) {
            if (buf1[i] != buf2[i]) {
                same = false;
            }
        }
        assertTrue("Didn't get the same data", same);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Illegal argument checks.",
        method = "read",
        args = {byte[].class, int.class, int.class}
    )      
    public void test_read$BII_Exception() throws IOException {
        DigestInputStream is = new DigestInputStream(inStream, digest);
        byte[] buf = null;
        try {
            is.read(buf, -1, 0);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
        } 
        buf = new byte[1000];        
        try {
            is.read(buf, -1, 0);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            is.read(buf, 0, -1);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        } 
        try {
            is.read(buf, -1, -1);
            fail("Test 4: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        } 
        try {
            is.read(buf, 0, 1001);
            fail("Test 5: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        } 
        try {
            is.read(buf, 1001, 0);
            fail("Test 6: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        } 
        try {
            is.read(buf, 500, 501);
            fail("Test 7: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        } 
        is.close();
        Support_ASimpleInputStream sis = new Support_ASimpleInputStream(true);
        is = new DigestInputStream(sis, digest);
        try {
            is.read(buf, 0, 100);
            fail("Test 9: IOException expected.");
        } catch (IOException e) {
        }
        sis.throwExceptionOnNextUse = false;
        is.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setMessageDigest",
        args = {java.security.MessageDigest.class}
    )
    public void test_setMessageDigestLjava_security_MessageDigest() {
        DigestInputStream dis = new DigestInputStream(inStream, null);
        assertNull(
                "Uninitialised MessageDigest should have been returned as null",
                dis.getMessageDigest());
        dis.setMessageDigest(digest);
        assertEquals("Wrong MessageDigest was returned.", digest, dis
                .getMessageDigest());
    }
    protected void setUp() {
        inStream = new ByteArrayInputStream(
                "This is a test string for digesting".getBytes());
        inStream1 = new ByteArrayInputStream(
                "This is a test string for digesting".getBytes());
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            fail("Unable to find SHA-1 algorithm");
        }
    }
}
