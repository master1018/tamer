@TestTargetClass(CipherOutputStream.class)
public class CipherOutputStream1Test extends TestCase {
    private static class TestOutputStream extends ByteArrayOutputStream {
        private boolean closed = false;
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
        method = "CipherOutputStream",
        args = {java.io.OutputStream.class}
    )
    public void testCipherOutputStream() throws Exception {
        byte[] data = new byte[] { -127, -100, -50, -10, -1, 0, 1, 10, 50, 127 };
        TestOutputStream tos = new TestOutputStream();
        CipherOutputStream cos = new CipherOutputStream(tos){};
        cos.write(data);
        cos.flush();
        byte[] result = tos.toByteArray();
        if (!Arrays.equals(result, data)) {
            fail("NullCipher should be used " + "if Cipher is not specified.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Can not check IOException.",
        method = "write",
        args = {int.class}
    )
    public void testWrite1() throws Exception {
        byte[] data = new byte[] { -127, -100, -50, -10, -1, 0, 1, 10, 50, 127 };
        TestOutputStream tos = new TestOutputStream();
        CipherOutputStream cos = new CipherOutputStream(tos, new NullCipher());
        for (int i = 0; i < data.length; i++) {
            cos.write(data[i]);
        }
        cos.flush();
        byte[] result = tos.toByteArray();
        if (!Arrays.equals(result, data)) {
            fail("CipherOutputStream wrote incorrect data.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Can not check IOException.",
        method = "write",
        args = {byte[].class}
    )
    public void testWrite2() throws Exception {
        byte[] data = new byte[] { -127, -100, -50, -10, -1, 0, 1, 10, 50, 127 };
        TestOutputStream tos = new TestOutputStream();
        CipherOutputStream cos = new CipherOutputStream(tos, new NullCipher());
        cos.write(data);
        cos.flush();
        byte[] result = tos.toByteArray();
        if (!Arrays.equals(result, data)) {
            fail("CipherOutputStream wrote incorrect data.");
        }
        try {
            cos.write(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Can not check IOException.",
        method = "write",
        args = {byte[].class, int.class, int.class}
    )
    public void testWrite3() throws Exception {
        byte[] data = new byte[] { -127, -100, -50, -10, -1, 0, 1, 10, 50, 127 };
        TestOutputStream tos = new TestOutputStream();
        CipherOutputStream cos = new CipherOutputStream(tos, new NullCipher());
        for (int i = 0; i < data.length; i++) {
            cos.write(data, i, 1);
        }
        cos.flush();
        byte[] result = tos.toByteArray();
        if (!Arrays.equals(result, data)) {
            fail("CipherOutputStream wrote incorrect data.");
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Regression test. IllegalArgumentException checked.",
        method = "write",
        args = {byte[].class, int.class, int.class}
    )
    public void testWrite4() throws Exception {
        try {
            new CipherOutputStream(new BufferedOutputStream((OutputStream) null), new NullCipher()).write(new byte[] {0}, 1, Integer.MAX_VALUE);
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Can not check IOException.",
        method = "write",
        args = {byte[].class, int.class, int.class}
    )
    public void testWrite5() throws Exception {
        Cipher cf = Cipher.getInstance("DES/CBC/PKCS5Padding");
        NullCipher nc = new NullCipher();
        CipherOutputStream stream1 = new CipherOutputStream(new BufferedOutputStream((OutputStream) null), nc);
        CipherOutputStream stream2 = new CipherOutputStream(stream1, cf);
        CipherOutputStream stream3 = new CipherOutputStream(stream2, nc);
        stream3.write(new byte[] {0}, 0, 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Can not check IOException.",
        method = "flush",
        args = {}
    )
    public void testFlush() throws Exception {
        byte[] data = new byte[] { -127, -100, -50, -10, -1, 0, 1, 10, 50, 127 };
        TestOutputStream tos = new TestOutputStream();
        CipherOutputStream cos = new CipherOutputStream(tos){};
        cos.write(data);
        cos.flush();
        byte[] result = tos.toByteArray();
        if (!Arrays.equals(result, data)) {
            fail("CipherOutputStream did not flush the data.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Can not check IOException.",
        method = "close",
        args = {}
    )
    public void testClose() throws Exception {
        byte[] data = new byte[] { -127, -100, -50, -10, -1, 0, 1, 10, 50, 127 };
        TestOutputStream tos = new TestOutputStream();
        CipherOutputStream cos = new CipherOutputStream(tos){};
        cos.write(data);
        cos.close();
        byte[] result = tos.toByteArray();
        if (!Arrays.equals(result, data)) {
            fail("CipherOutputStream did not flush the data.");
        }
        assertTrue("The close() method should call the close() method "
                + "of its underlying output stream.", tos.wasClosed());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CipherOutputStream",
        args = {java.io.OutputStream.class, javax.crypto.Cipher.class}
    )
    public void test_ConstructorLjava_io_OutputStreamLjavax_crypto_Cipher() throws
    NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        KeyGenerator kg = KeyGenerator.getInstance("DES");
        kg.init(56, new SecureRandom());
        Key key = kg.generateKey();
        Cipher c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, key);
        CipherOutputStream cos = new CipherOutputStream(baos, c);
        assertNotNull(cos);
    }
}
