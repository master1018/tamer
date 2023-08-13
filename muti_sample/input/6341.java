public class TryLock {
    public static void main(String[] args) throws Exception {
        test1(true, true);
        test1(false, true);
        test1(true, false);
        test1(false, false);
        test2(true, true);
        test2(false, true);
        test2(true, false);
        test2(false, false);
        test3(true, true);
        test3(false, true);
        test3(true, false);
        test3(false, false);
    }
    public static void test1(boolean shared, boolean trylock) throws Exception {
        File testFile = File.createTempFile("test1", null);
        FileInputStream fis = new FileInputStream(testFile);
        FileChannel fc = fis.getChannel();
        FileLock fl = null;
        try {
            if (trylock)
                fl = fc.tryLock(0, fc.size(), shared);
            else
                fl = fc.lock(0, fc.size(), shared);
            if (!shared)
                throw new RuntimeException("No exception thrown for test1");
        } catch (NonWritableChannelException e) {
            if (shared)
                throw new RuntimeException("Exception thrown for wrong case test1");
        } finally {
            if (fl != null)
                fl.release();
            fc.close();
            testFile.delete();
        }
    }
    public static void test2(boolean shared, boolean trylock) throws Exception {
        File testFile = File.createTempFile("test2", null);
        FileOutputStream fis = new FileOutputStream(testFile);
        FileChannel fc = fis.getChannel();
        FileLock fl = null;
        try {
            if (trylock)
                fl = fc.tryLock(0, fc.size(), shared);
            else
                fl = fc.lock(0, fc.size(), shared);
            if (shared)
                throw new RuntimeException("No exception thrown for test2");
        } catch (NonReadableChannelException e) {
            if (!shared)
                throw new RuntimeException("Exception thrown incorrectly for test2");
        } finally {
            if (fl != null)
                fl.release();
            fc.close();
            testFile.delete();
        }
    }
    public static void test3(boolean shared, boolean trylock) throws Exception {
        File testFile = File.createTempFile("test3", null);
        RandomAccessFile fis = new RandomAccessFile(testFile, "rw");
        FileChannel fc = fis.getChannel();
        try {
            FileLock fl = null;
            if (trylock)
                fl = fc.tryLock(0, fc.size(), shared);
            else
                fl = fc.lock(0, fc.size(), shared);
            fl.release();
        } finally {
            fc.close();
            testFile.delete();
        }
    }
}
