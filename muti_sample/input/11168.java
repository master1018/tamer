public class TestShortBuffer {
    private static final String ALGO = "AES";
    private static final String[] MODES = {
        "ECB", "CBC", "PCBC", "CFB16", "OFB8"
    };
    private static final SecretKey KEY =
        new SecretKeySpec(new byte[16], ALGO);
    private static byte[] SHORTBUFFER = new byte[1];
    private static final byte[] PLAINTEXT  = new byte[30];
    static {
        PLAINTEXT[0] = (byte)0x15;
    };
    private Cipher ci = null;
    private byte[] in = null;
    private byte[] expected = null;
    private byte[] out = null;
    private int outOffset = 0;
    private TestShortBuffer(Cipher ci) {
        this.ci = ci;
    }
    private void init(byte[] in, byte[] expected) {
        this.in = (byte[]) in.clone();
        this.expected = (byte[]) expected.clone();
        this.out = new byte[expected.length];
        this.outOffset = 0;
   }
    private static void runTest() throws Exception {
        for (int i = 0; i < MODES.length; i++) {
            System.out.println("===== TESTING MODE " + MODES[i] + " =====");
            Cipher ci = Cipher.getInstance(ALGO+"/"+MODES[i]+"/PKCS5Padding",
                                           "SunJCE");
            TestShortBuffer test = null;
            int stored = 0;
            AlgorithmParameters params = null;
            byte[] cipherText = null;
            byte[] shortBuffer = new byte[8];
            for (int k = 0; k < 2; k++) {
                byte[] expected = null;
                switch (k) {
                case 0: 
                    System.out.println("Testing with Cipher.ENCRYPT_MODE");
                    ci.init(Cipher.ENCRYPT_MODE, KEY);
                    cipherText = ci.doFinal(PLAINTEXT);
                    test = new TestShortBuffer(ci);
                    test.init(PLAINTEXT, cipherText);
                    params = ci.getParameters();
                    break;
                case 1: 
                    System.out.println("Testing with Cipher.DECRYPT_MODE");
                    ci.init(Cipher.DECRYPT_MODE, KEY, params);
                    test = new TestShortBuffer(ci);
                    test.init(cipherText, PLAINTEXT);
                    break;
                }
                int offset = 2 + i*5;
                test.testUpdate();
                test.testUpdateWithUpdate(offset);
                test.testDoFinal();
                test.testDoFinalWithUpdate(offset);
            }
        }
    }
    private void checkOutput() throws Exception {
        if (!Arrays.equals(out, expected)) {
            System.out.println("got: " + new BigInteger(out));
            System.out.println("expect: " + new BigInteger(expected));
            throw new Exception("Generated different outputs");
        }
    }
    private void testUpdate() throws Exception {
        outOffset = 0;
        int stored = 0;
        try {
            stored = ci.update(in, 0, in.length, SHORTBUFFER);
            throw new Exception("Should throw ShortBufferException!");
        } catch (ShortBufferException sbe) {
            System.out.println("Expected SBE thrown....");
            stored = ci.update(in, 0, in.length, out);
            stored = ci.doFinal(out, outOffset += stored);
            if (out.length != (stored + outOffset)) {
                throw new Exception("Wrong number of output bytes");
            }
        }
        checkOutput();
    }
    private void testUpdateWithUpdate(int offset) throws Exception {
        outOffset = 0;
        int stored = 0;
        byte[] out1 = ci.update(in, 0, offset);
        if (out1 != null) {
            System.arraycopy(out1, 0, out, 0, out1.length);
            outOffset += out1.length;
        }
        try {
            stored = ci.update(in, offset, in.length-offset, SHORTBUFFER);
            throw new Exception("Should throw ShortBufferException!");
        } catch (ShortBufferException sbe) {
            System.out.println("Expected SBE thrown....");
            stored = ci.update(in, offset, in.length-offset,
                               out, outOffset);
            stored = ci.doFinal(out, outOffset+=stored);
            if (out.length != (stored + outOffset)) {
                throw new Exception("Wrong number of output bytes");
            }
        }
        checkOutput();
    }
    private void testDoFinal() throws Exception {
        outOffset = 0;
        int stored = 0;
        try {
            stored = ci.doFinal(in, 0, in.length, SHORTBUFFER);
            throw new Exception("Should throw ShortBufferException!");
        } catch (ShortBufferException sbe) {
            System.out.println("Expected SBE thrown....");
            stored = ci.doFinal(in, 0, in.length, out, 0);
            if (out.length != stored) {
                throw new Exception("Wrong number of output bytes");
            }
        }
        checkOutput();
    }
    private void testDoFinalWithUpdate(int offset) throws Exception {
        outOffset = 0;
        int stored = 0;
        byte[] out1 = ci.update(in, 0, offset);
        if (out1 != null) {
            System.arraycopy(out1, 0, out, 0, out1.length);
            outOffset += out1.length;
        }
        try {
            stored = ci.doFinal(in, offset, in.length-offset, SHORTBUFFER);
            throw new Exception("Should throw ShortBufferException!");
        } catch (ShortBufferException sbe) {
            System.out.println("Expected SBE thrown....");
            stored = ci.doFinal(in, offset, in.length-offset,
                                out, outOffset);
            if (out.length != (stored + outOffset)) {
                throw new Exception("Wrong number of output bytes");
            }
        }
        checkOutput();
    }
    public static void main(String[] argv) throws Exception {
        runTest();
        System.out.println("Test Passed");
    }
}
