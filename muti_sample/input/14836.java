public class BashCache {
    private static final int THREADS = 10;
    private static final int TRIALS = 1000;
    private static final Charset[] charsets
        = new Charset[] {
            Charset.forName("US-ASCII"),
            Charset.forName("UTF-8"),
            Charset.forName("CP1252"),
            Charset.forName("UTF-16BE") };
    private static volatile boolean failed = false;
    private static class Basher extends Thread {
        Random rnd = new Random(System.identityHashCode(this));
        public void run() {
            for (int i = 0; i < TRIALS; i++) {
                Charset cs = charsets[rnd.nextInt(4)];
                try {
                    if (rnd.nextBoolean()) {
                        cs.encode("hi mom");
                    } else {
                        cs.decode(ByteBuffer.wrap(new byte[] {
                            (byte)'x', (byte)'y',
                            (byte)'z', (byte)'z',
                            (byte)'y' }));
                    }
                } catch (Exception x) {
                    x.printStackTrace();
                    failed = true;
                    return;
                }
                if (rnd.nextBoolean())
                    Thread.yield();
            }
        }
    }
    public static void main(String[] args) throws Exception {
        Charset cs = Charset.forName("us-ascii");
        Basher[] bashers = new Basher[THREADS];
        for (int i = 0; i < THREADS; i++) {
            bashers[i] = new Basher();
            bashers[i].start();
        }
        for (int i = 0; i < THREADS; i++)
            bashers[i].join();
        if (failed)
            throw new Exception("Test failed");
    }
}
