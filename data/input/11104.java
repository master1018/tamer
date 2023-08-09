public class CopyDirectByteMemory
    extends CopyDirectMemory
{
    private static void init(ByteBuffer b) {
        int n = b.capacity();
        b.clear();
        for (int i = 0; i < n; i++)
            b.put(i, (byte)ic(i));
        b.limit(n);
        b.position(0);
    }
    private static void init(byte [] a) {
        for (int i = 0; i < a.length; i++)
            a[i] = (byte)ic(i + 1);
    }
    public static void test() {
        ByteBuffer b = ByteBuffer.allocateDirect(1024 * 1024 + 1024);
        init(b);
        byte [] a = new byte[b.capacity()];
        init(a);
        b.put(a);
        for (int i = 0; i < a.length; i++)
            ck(b, b.get(i), (byte)ic(i + 1));
        init(b);
        init(a);
        b.get(a);
        for (int i = 0; i < a.length; i++)
            if (a[i] != b.get(i))
                fail("Copy failed at " + i + ": '"
                     + a[i] + "' != '" + b.get(i) + "'");
    }
}
