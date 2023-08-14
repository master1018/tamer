public class ReleaseOnCloseDeadlock {
    private static final int LOCK_COUNT = 1024;
    public static void main(String[] args) throws IOException {
        File blah = File.createTempFile("blah", null);
        blah.deleteOnExit();
        try {
            for (int i=0; i<100; i++) {
                test(blah.toPath());
            }
        } finally {
            blah.delete();
        }
    }
    static void test(Path file) throws IOException {
        FileLock[] locks = new FileLock[LOCK_COUNT];
        FileChannel fc = FileChannel.open(file, READ, WRITE);
        for (int i=0; i<LOCK_COUNT; i++) {
            locks[i] = fc.lock(i, 1, true);
        }
        tryToDeadlock(fc, locks);
        AsynchronousFileChannel ch = AsynchronousFileChannel.open(file, READ, WRITE);
        for (int i=0; i<LOCK_COUNT; i++) {
            try {
                locks[i] = ch.lock(i, 1, true).get();
            } catch (InterruptedException x) {
                throw new RuntimeException(x);
            } catch (ExecutionException x) {
                throw new RuntimeException(x);
            }
        }
        tryToDeadlock(ch, locks);
    }
    static void tryToDeadlock(final Channel channel, FileLock[] locks)
        throws IOException
    {
        Thread closer = new Thread(
            new Runnable() {
                public void run() {
                    try {
                        channel.close();
                    } catch (IOException ignore) {
                        ignore.printStackTrace();
                    }
                }});
        closer.start();
        for (int i=0; i<locks.length; i++) {
            try {
                locks[i].release();
            } catch (ClosedChannelException ignore) { }
        }
        while (closer.isAlive()) {
            try {
                closer.join();
            } catch (InterruptedException ignore) { }
        }
    }
}
