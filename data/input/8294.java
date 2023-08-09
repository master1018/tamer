public class CopyDirectDoubleMemory
    extends CopyDirectMemory
{
    private static void init(DoubleBuffer b) {
        int n = b.capacity();
        b.clear();
        for (int i = 0; i < n; i++)
            b.put(i, (double)ic(i));
        b.limit(n);
        b.position(0);
    }
    private static void init(double [] a) {
        for (int i = 0; i < a.length; i++)
            a[i] = (double)ic(i + 1);
    }
    public static void test() {
        ByteBuffer bb = ByteBuffer.allocateDirect(1024 * 1024 + 1024);
        DoubleBuffer b = bb.asDoubleBuffer();
        init(b);
        double [] a = new double[b.capacity()];
        init(a);
        b.put(a);
        for (int i = 0; i < a.length; i++)
            ck(b, b.get(i), (double)ic(i + 1));
        init(b);
        init(a);
        b.get(a);
        for (int i = 0; i < a.length; i++)
            if (a[i] != b.get(i))
                fail("Copy failed at " + i + ": '"
                     + a[i] + "' != '" + b.get(i) + "'");
    }
}
