public class MethodCall extends MethodCallBase {
    MethodCall() {
        super();
        System.out.println("  MethodCall ctor");
    }
    int tryThing() {
        int val = super.tryThing();
        assert(val == 7);
        return val;
    }
    private void directly() {}
    static void manyArgs(int a0, long a1, int a2, long a3, int a4, long a5,
        int a6, int a7, double a8, float a9, double a10, short a11, int a12,
        char a13, int a14, int a15, byte a16, boolean a17, int a18, int a19,
        long a20, long a21, int a22, int a23, int a24, int a25, int a26,
        String[][] a27, String[] a28, String a29)
    {
        System.out.println("MethodCalls.manyArgs");
        assert(a0 == 0);
        assert(a9 > 8.99 && a9 < 9.01);
        assert(a16 == -16);
        assert(a25 == 25);
        assert(a29.equals("twenty nine"));
    }
    public static void run() {
        MethodCall inst = new MethodCall();
        MethodCallBase base = inst;
        base.tryThing();
        inst.tryThing();
        inst = null;
        try {
            inst.directly();
            assert(false);
        } catch (NullPointerException npe) {
        }
        manyArgs(0, 1L, 2, 3L, 4, 5L, 6, 7, 8.0, 9.0f, 10.0, (short)11, 12,
            (char)13, 14, 15, (byte)-16, true, 18, 19, 20L, 21L, 22, 23, 24,
            25, 26, null, null, "twenty nine");
    }
}
class MethodCallBase {
    MethodCallBase() {
        System.out.println("  MethodCallBase ctor");
    }
    int tryThing() {
        return 7;
    }
}
