public class FlaterTest extends Thread {
    private static final int DATA_LEN = 1024 * 128;
    private static byte[] data;
    private static final boolean debug = false;
    private static Set flaters =
        Collections.synchronizedSet(new HashSet());
    static void createData() {
        ByteBuffer bb = ByteBuffer.allocate(8);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < DATA_LEN; i++) {
            bb.putDouble(0, Math.random());
            baos.write(bb.array(), 0, 8);
        }
        data = baos.toByteArray();
        if (debug) System.out.println("data length is " + data.length);
    }
    private static int getDeflatedLength() throws Throwable {
        int rc = 0;
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();
        byte[] out = new byte[data.length];
        rc = deflater.deflate(out);
        deflater.end();
        if (debug) System.out.println("deflatedLength is " + rc);
        return rc;
    }
    private static void validate(byte[] buf, int offset, int len) throws Exception {
        for (int i = 0; i < len; i++ ) {
            if (buf[i] != data[offset+i]) {
                throw new Exception("mismatch at " + (offset + i));
            }
        }
    }
    public static void realMain(String[] args) throws Throwable {
        createData();
        int numThreads = args.length > 0 ? Integer.parseInt(args[0]) : 5;
        new FlaterTest().go(numThreads);
    }
    synchronized private void go(int numThreads) throws Throwable {
        int deflatedLength = getDeflatedLength();
        long time = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++) {
            Flater f = new Flater(deflatedLength);
            flaters.add(f);
            f.start();
        }
        while (flaters.size() != 0) {
            try {
                Thread.currentThread().sleep(10);
            } catch (InterruptedException ex) {
                unexpected(ex);
            }
        }
        time = System.currentTimeMillis() - time;
        System.out.println("Time needed for " + numThreads
                           + " threads to deflate/inflate: " + time + " ms.");
    }
    static class Flater extends Thread {
        private final int deflatedLength;
        private Flater(int length) {
            this.deflatedLength = length;
        }
        public void run() {
            if (debug) System.out.println(getName() + " starting run()");
            try {
                byte[] deflated = DeflateData(deflatedLength);
                InflateData(deflated);
            } catch (Throwable t) {
                t.printStackTrace();
                fail(getName() + " failed");
            } finally {
                flaters.remove(this);
            }
        }
        private byte[] DeflateData(int length) throws Throwable {
            Deflater deflater = new Deflater();
            deflater.setInput(data);
            deflater.finish();
            byte[] out = new byte[length];
            deflater.deflate(out);
            return out;
        }
        private void InflateData(byte[] bytes) throws Throwable {
            Inflater inflater = new Inflater();
            inflater.setInput(bytes, 0, bytes.length);
            int len = 1024 * 8;
            int offset = 0;
            while (inflater.getRemaining() > 0) {
                byte[] buf = new byte[len];
                int inflated = inflater.inflate(buf, 0, len);
                validate(buf, offset, inflated);
                offset += inflated;
            }
        }
    }
    static volatile int passed = 0, failed = 0;
    static void pass() {passed++;}
    static void fail() {failed++; Thread.dumpStack();}
    static void fail(String msg) {System.out.println(msg); fail();}
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    static void check(boolean cond) {if (cond) pass(); else fail();}
    static void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.println("\nPassed = " + passed + " failed = " + failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
