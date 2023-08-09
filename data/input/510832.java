public class CryptoTest extends TestCase {
    public void doTestMessageDigest(Digest oldDigest, Digest newDigest) {
        final int ITERATIONS = 10;
        byte[] data = new byte[1024];
        byte[] oldHash = new byte[oldDigest.getDigestSize()];
        byte[] newHash = new byte[newDigest.getDigestSize()];
        Assert.assertEquals("Hash names must be equal", oldDigest.getAlgorithmName(), newDigest.getAlgorithmName());
        Assert.assertEquals("Hash sizes must be equal", oldHash.length, newHash.length);
        Assert.assertEquals("Hash block sizes must be equal", ((ExtendedDigest)oldDigest).getByteLength(), ((ExtendedDigest)newDigest).getByteLength());
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)i;
        }
        long oldTime = 0;
        long newTime = 0;
        for (int j = 0; j < ITERATIONS; j++) {
            long t0 = System.currentTimeMillis();
            for (int i = 0; i < 4; i++) {
                oldDigest.update(data, 0, data.length);
            }
            int oldLength = oldDigest.doFinal(oldHash, 0);
            long t1 = System.currentTimeMillis();
            oldTime = oldTime + (t1 - t0);
            long t2 = System.currentTimeMillis();
            for (int i = 0; i < 4; i++) {
                newDigest.update(data, 0, data.length);
            }
            int newLength = newDigest.doFinal(newHash, 0);
            long t3 = System.currentTimeMillis();
            newTime = newTime + (t3 - t2);
            Assert.assertEquals("Hash sizes must be equal", oldLength, newLength);
            for (int i = 0; i < oldLength; i++) {
                Assert.assertEquals("Hashes[" + i + "] must be equal", oldHash[i], newHash[i]);
            }
        }
        android.util.Log.d("CryptoTest", "Time for " + ITERATIONS + " x old hash processing: " + oldTime + " ms");
        android.util.Log.d("CryptoTest", "Time for " + ITERATIONS + " x new hash processing: " + newTime + " ms");
    }
    @MediumTest
    public void testMD4() {
        Digest oldDigest = new MD4Digest();
        Digest newDigest = OpenSSLMessageDigest.getInstance("MD4");
        doTestMessageDigest(oldDigest, newDigest);
    }
    @MediumTest
    public void testMD5() {
        Digest oldDigest = new MD5Digest();
        Digest newDigest = OpenSSLMessageDigest.getInstance("MD5");
        doTestMessageDigest(oldDigest, newDigest);
    }
    @MediumTest
    public void testSHA1() {
        Digest oldDigest = new SHA1Digest();
        Digest newDigest = OpenSSLMessageDigest.getInstance("SHA-1");
        doTestMessageDigest(oldDigest, newDigest);
    }
}
