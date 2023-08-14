@TestTargetClass(CipherInputStream.class)
public class CipherInputStream1Test extends TestCase {
    private static class TestInputStream extends ByteArrayInputStream {
        private boolean closed = false;
        public TestInputStream(byte[] data) {
            super(data);
        }
        public void close() {
            closed = true;
        }
        public boolean wasClosed() {
            return closed;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CipherInputStream",
        args = {java.io.InputStream.class}
    )
    public void testCipherInputStream() throws Exception {
        byte[] data = new byte[] { -127, -100, -50, -10, -1, 0, 1, 10, 50, 127 };
        TestInputStream tis = new TestInputStream(data);
        CipherInputStream cis = new CipherInputStream(tis){};
        for (int i = 0; i < data.length; i++) {
            if ((byte) cis.read() != data[i]) {
                fail("NullCipher should be used "
                        + "if Cipher is not specified.");
            }
        }
        if (cis.read() != -1) {
            fail("NullCipher should be used if Cipher is not specified.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Can not check IOException.",
        method = "read",
        args = {}
    )
    public void testRead1() throws Exception {
        byte[] data = new byte[] { -127, -100, -50, -10, -1, 0, 1, 10, 50, 127 };
        TestInputStream tis = new TestInputStream(data);
        CipherInputStream cis = new CipherInputStream(tis, new NullCipher());
        byte res;
        for (int i = 0; i < data.length; i++) {
            if ((res = (byte) cis.read()) != data[i]) {
                fail("read() returned the incorrect value. " + "Expected: "
                        + data[i] + ", Got: " + res + ".");
            }
        }
        if (cis.read() != -1) {
            fail("read() should return -1 at the end of the stream.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Can not check IOException.",
        method = "read",
        args = {byte[].class}
    )
    public void testRead2() throws Exception {
        byte[] data = new byte[] { -127, -100, -50, -10, -1, 0, 1, 10, 50, 127 };
        TestInputStream tis = new TestInputStream(data);
        CipherInputStream cis = new CipherInputStream(tis, new NullCipher());
        int expected = data.length;
        byte[] result = new byte[expected];
        int ind = 0; 
        int got = cis.read(result); 
        while (true) {
            for (int j = 0; j < got - ind; j++) {
                if (result[j] != data[ind + j]) {
                    fail("read(byte[] b) returned incorrect data.");
                }
            }
            if (got == expected) {
                break;
            } else if (got > expected) {
                fail("The data returned by read(byte[] b) "
                        + "is larger than expected.");
            } else {
                ind = got;
                got += cis.read(result);
            }
        }
        if (cis.read(result) != -1) {
            fail("read(byte[] b) should return -1 "
                    + "at the end of the stream.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Can not check IOException.",
        method = "read",
        args = {byte[].class, int.class, int.class}
    )
    public void testRead3() throws Exception {
        byte[] data = new byte[] { -127, -100, -50, -10, -1, 0, 1, 10, 50, 127 };
        TestInputStream tis = new TestInputStream(data);
        CipherInputStream cis = new CipherInputStream(tis, new NullCipher());
        int expected = data.length;
        byte[] result = new byte[expected];
        int skip = 2;
        int ind = skip; 
        cis.read(null, 0, skip);
        int got = skip + cis.read(result, 0, 1); 
        while (true) {
            for (int j = 0; j < got - ind; j++) {
                assertEquals("read(byte[] b, int off, int len) "
                        + "returned incorrect data.", result[j], data[ind + j]);
            }
            if (got == expected) {
                break;
            } else if (got > expected) {
                fail("The data returned by "
                        + "read(byte[] b, int off, int len) "
                        + "is larger than expected.");
            } else {
                ind = got;
                got += cis.read(result, 0, 3);
            }
        }
        if (cis.read(result, 0, 1) != -1) {
            fail("read() should return -1 at the end of the stream.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Can not check IOException.",
        method = "skip",
        args = {long.class}
    )
    public void testSkip() throws Exception {
        byte[] data = new byte[] { -127, -100, -50, -10, -1, 0, 1, 10, 50, 127 };
        TestInputStream tis = new TestInputStream(data);
        CipherInputStream cis = new CipherInputStream(tis, new NullCipher());
        int expected = data.length;
        byte[] result = new byte[expected];
        int skipped = (int) cis.skip(2);
        int ind = skipped;
        int got = skipped + cis.read(result, 0, 1); 
        while (true) {
            for (int j = 0; j < got - ind; j++) {
                if (result[j] != data[ind + j]) {
                    fail("read(byte[] b, int off, int len) "
                            + "returned incorrect data: Expected "
                            + data[ind + j] + ", got: " + result[j]);
                }
            }
            if (got == expected) {
                break;
            } else if (got > expected) {
                fail("The data returned by "
                        + "read(byte[] b, int off, int len) "
                        + "is larger than expected.");
            } else {
                ind = got;
                got += cis.read(result, 0, 1);
            }
        }
        if ((got = cis.read(result, 0, 1)) != -1) {
            fail("read() should return -1 at the end of the stream. "
                    + "Output is: " + got + ".");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Can not check IOException.",
        method = "available",
        args = {}
    )
    public void testAvailable() throws Exception {
        byte[] data = new byte[] { -127, -100, -50, -10, -1, 0, 1, 10, 50, 127 };
        TestInputStream tis = new TestInputStream(data);
        CipherInputStream cis = new CipherInputStream(tis, new NullCipher());
        assertEquals("The returned by available() method value "
                + "should be 0.", cis.available(), 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Can not check IOException.",
        method = "close",
        args = {}
    )
    public void testClose() throws Exception {
        byte[] data = new byte[] { -127, -100, -50, -10, -1, 0, 1, 10, 50, 127 };
        TestInputStream tis = new TestInputStream(data);
        CipherInputStream cis = new CipherInputStream(tis, new NullCipher());
        cis.close();
        assertTrue("The close() method should call the close() method "
                + "of its underlying input stream.", tis.wasClosed());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "markSupported",
        args = {}
    )
    public void testMarkSupported() {
        byte[] data = new byte[] {-127, -100, -50, -10, -1, 0, 1, 10, 50, 127};
        TestInputStream tis = new TestInputStream(data);
        CipherInputStream cis = new CipherInputStream(tis, new NullCipher());
        assertFalse("The returned by markSupported() method value "
                + "should be false.", cis.markSupported());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CipherInputStream",
        args = {java.io.InputStream.class, javax.crypto.Cipher.class}
    )
    public void test_ConstructorLjava_io_InputStreamLjavax_crypto_Cipher () throws 
    NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[100]);
        KeyGenerator kg = KeyGenerator.getInstance("DES");
        kg.init(56, new SecureRandom());
        Key key = kg.generateKey();
        Cipher c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, key);
        CipherInputStream cis = new CipherInputStream(bais, c);
        assertNotNull(cis);
    }
}
