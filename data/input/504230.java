public class Sha1Test extends TestCase {
    class TestData {
        private String input;
        private String result;
        public TestData(String i, String r) {
            input = i;
            result = r;
        }
    }
    TestData[] mTestData = new TestData[]{
            new TestData("abc", "a9993e364706816aba3e25717850c26c9cd0d89d"),
            new TestData("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq",
                    "84983e441c3bd26ebaae4aa1f95129e5e54670f1")
    };
    @SmallTest
    public void testSha1() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        int numTests = mTestData.length;
        for (int i = 0; i < numTests; i++) {
            digest.update(mTestData[i].input.getBytes());
            byte[] hash = digest.digest();
            String encodedHash = encodeHex(hash);
            assertEquals(encodedHash, mTestData[i].result);
        }
    }
    private static String encodeHex(byte[] bytes) {
        StringBuffer hex = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            if (((int) bytes[i] & 0xff) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toString((int) bytes[i] & 0xff, 16));
        }
        return hex.toString();
    }
}
