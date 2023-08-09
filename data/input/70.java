public class CopyDirectCharMemory
    extends CopyDirectMemory
{
    private static void init(CharBuffer b) {
        int n = b.capacity();
        b.clear();
        for (int i = 0; i < n; i++)
            b.put(i, (char)ic(i));
        b.limit(n);
        b.position(0);
    }
    private static void init(char [] a) {
        for (int i = 0; i < a.length; i++)
            a[i] = (char)ic(i + 1);
    }
    public static void test() {
        ByteBuffer bb = ByteBuffer.allocateDirect(1024 * 1024 + 1024);
        CharBuffer b = bb.asCharBuffer();
        init(b);
        char [] a = new char[b.capacity()];
        init(a);
        b.put(a);
        for (int i = 0; i < a.length; i++)
            ck(b, b.get(i), (char)ic(i + 1));
        init(b);
        init(a);
        b.get(a);
        for (int i = 0; i < a.length; i++)
            if (a[i] != b.get(i))
                fail("Copy failed at " + i + ": '"
                     + a[i] + "' != '" + b.get(i) + "'");
    }
}
