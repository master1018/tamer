public class DeflaterOutputStreamTest extends TestCase {
    public void testSyncFlushEnabled() throws Exception {
        InflaterInputStream in = createInflaterStream(true);
        assertEquals(1, in.read());
        assertEquals(2, in.read());
        assertEquals(3, in.read());
    }
    public void testSyncFlushDisabled() throws Exception {
        InflaterInputStream in = createInflaterStream(false);
        try {
            in.read();
            fail();
        } catch (IOException expected) {
        }
    }
    private InflaterInputStream createInflaterStream(final boolean flushing)
            throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final PipedOutputStream pout = new PipedOutputStream();
        PipedInputStream pin = new PipedInputStream(pout);
        executor.submit(new Callable<Void>() {
            public Void call() throws Exception {
                OutputStream out = new DeflaterOutputStream(pout, flushing);
                out.write(1);
                out.write(2);
                out.write(3);
                out.flush();
                return null;
            }
        }).get();
        executor.shutdown();
        return new InflaterInputStream(pin);
    }
}
