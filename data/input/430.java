public class CopyDirectLongMemory
    extends CopyDirectMemory
{
    private static void init(LongBuffer b) {
        int n = b.capacity();
        b.clear();
        for (int i = 0; i < n; i++)
            b.put(i, (long)ic(i));
        b.limit(n);
        b.position(0);
    }
    private static void init(long [] a) {
        for (int i = 0; i < a.length; i++)
            a[i] = (long)ic(i + 1);
    }
    public static void test() {
        ByteBuffer bb = ByteBuffer.allocateDirect(1024 * 1024 + 1024);
        LongBuffer b = bb.asLongBuffer();
        init(b);
        long [] a = new long[b.capacity()];
        init(a);
        b.put(a);
        for (int i = 0; i < a.length; i++)
            ck(b, b.get(i), (long)ic(i + 1));
        init(b);
        init(a);
        b.get(a);
        for (int i = 0; i < a.length; i++)
            if (a[i] != b.get(i))
                fail("Copy failed at " + i + ": '"
                     + a[i] + "' != '" + b.get(i) + "'");
    }
}
