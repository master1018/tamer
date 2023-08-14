public class ReadUByte {
    static class BrokenInputStream extends ByteArrayInputStream {
        BrokenInputStream() {
            super(new byte[16]);
        }
        public int read() {
            return -33;
        }
    }
    public static void realMain(String[] args) throws Throwable {
        try {
            new GZIPInputStream(new BrokenInputStream());
            fail("Failed to throw expected IOException");
        } catch (IOException ex) {
            String msg = ex.getMessage();
            if (msg.indexOf("ReadUByte$BrokenInputStream.read() returned value out of range") < 0) {
                fail("IOException contains incorrect message: '" + msg + "'");
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
