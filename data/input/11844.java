public class Flush {
    private static byte[] contents(ByteBuffer bb) {
        byte[] contents = new byte[bb.position()];
        ((ByteBuffer)(bb.duplicate().flip())).get(contents);
        return contents;
    }
    private static ByteBuffer extend(ByteBuffer bb) {
        ByteBuffer x = ByteBuffer.allocate(2*bb.capacity()+10);
        bb.flip();
        x.put(bb);
        return x;
    }
    private static void realMain(String[] args) throws Throwable {
        char[] jis0208 = {'\u3001'};
        CharBuffer cb = CharBuffer.wrap(jis0208);
        ByteBuffer bb = ByteBuffer.allocate(6);
        CharsetEncoder enc = Charset.forName("ISO-2022-JP").newEncoder();
        check(enc.encode(cb, bb, true).isUnderflow());
        System.out.println(Arrays.toString(contents(bb)));
        check(! cb.hasRemaining());
        equal(contents(bb).length, 3 + 2);
        equal(bb.get(0), (byte)0x1b);
        check(enc.flush(bb).isOverflow());
        check(enc.flush(bb).isOverflow());
        equal(contents(bb).length, 3 + 2);
        bb = extend(bb);
        check(enc.flush(bb).isUnderflow());
        equal(bb.get(3 + 2), (byte)0x1b);
        System.out.println(Arrays.toString(contents(bb)));
        equal(contents(bb).length, 3 + 2 + 3);
        check(enc.flush(bb).isUnderflow());
        check(enc.flush(bb).isUnderflow());
        equal(contents(bb).length, 3 + 2 + 3);
        bb = enc.encode(CharBuffer.wrap(jis0208));
        byte[] expected = "\u001b$B!\"\u001b(B".getBytes("ASCII");
        byte[] contents = new byte[bb.limit()]; bb.get(contents);
        check(Arrays.equals(contents, expected));
    }
    static volatile int passed = 0, failed = 0;
    static void pass() { passed++; }
    static void fail() { failed++; Thread.dumpStack(); }
    static void fail(String msg) { System.out.println(msg); fail(); }
    static void unexpected(Throwable t) { failed++; t.printStackTrace(); }
    static void check(boolean cond) { if (cond) pass(); else fail(); }
    static void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else {System.out.println(x + " not equal to " + y); fail(); }}
    public static void main(String[] args) throws Throwable {
        try { realMain(args); } catch (Throwable t) { unexpected(t); }
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new Exception("Some tests failed");
    }
}
