public class FileClientSessionCacheTest extends TestCase {
    public void testMaxSize() throws IOException, InterruptedException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        if (tmpDir == null) {
            fail("Please set 'java.io.tmpdir' system property.");
        }
        File cacheDir = new File(tmpDir
                + "/" + FileClientSessionCacheTest.class.getName() + "/cache");
        final SSLClientSessionCache cache
                = FileClientSessionCache.usingDirectory(cacheDir);
        Thread[] threads = new Thread[10];
        final int iterations = FileClientSessionCache.MAX_SIZE * 10;
        for (int i = 0; i < threads.length; i++) {
            final int id = i;
            threads[i] = new Thread() {
                @Override
                public void run() {
                    for (int i = 0; i < iterations; i++) {
                        cache.putSessionData(new FakeSession(id + "." + i),
                                new byte[10]);
                    }
                }
            };
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        assertEquals(FileClientSessionCache.MAX_SIZE, cacheDir.list().length);
    }
}
