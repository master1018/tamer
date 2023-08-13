public class TestISO10126Padding {
    private static final String ALGO = "AES";
    private static final String TRANS = "AES/ECB";
    private static final int KEYSIZE = 16; 
    private SecretKey key;
    private TestISO10126Padding() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(ALGO, "SunJCE");
        kg.init(KEYSIZE*8);
        key = kg.generateKey();
    }
    private void runTest(int dataLength) throws Exception {
        byte[] data = new byte[dataLength];
        new SecureRandom().nextBytes(data);
        System.out.println("Testing data length: " + dataLength);
        Cipher ci = Cipher.getInstance(TRANS + "/NoPadding", "SunJCE");
        ci.init(Cipher.ENCRYPT_MODE, key);
        byte[] paddedData = new byte[ci.getBlockSize()];
        System.arraycopy(data, 0, paddedData, 0, data.length);
        int padValue = paddedData.length - data.length;
        paddedData[paddedData.length-1] = (byte) padValue;
        byte[] cipherText = ci.doFinal(paddedData);
        ci = Cipher.getInstance(TRANS + "/ISO10126Padding", "SunJCE");
        ci.init(Cipher.DECRYPT_MODE, key);
        byte[] recovered = ci.doFinal(cipherText);
        if (!Arrays.equals(data, recovered)) {
            throw new Exception("TEST#1: decryption failed");
        }
        ci = Cipher.getInstance(TRANS + "/ISO10126Padding", "SunJCE");
        ci.init(Cipher.ENCRYPT_MODE, key);
        cipherText = ci.doFinal(data);
        ci.init(Cipher.DECRYPT_MODE, key);
        recovered = ci.doFinal(cipherText);
        if (!Arrays.equals(data, recovered)) {
            throw new Exception("TEST#2: decryption failed");
        }
    }
    public static void main(String[] argv) throws Exception {
        TestISO10126Padding test = new TestISO10126Padding();
        for (int i = 0; i<16; i++) {
            test.runTest(i);
        }
        System.out.println("Test Passed");
    }
}
