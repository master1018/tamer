public class InternedString {
    public static final String CONST = "Class InternedString";
    public static void run() {
        System.out.println("InternedString.run");
        testImmortalInternedString();
        testDeadInternedString();
    }
    private static void testDeadInternedString() {
        String s = "blah";
        s = s + s;
        WeakReference strRef = new WeakReference<String>(s.intern());
        s = CONST;
        System.gc();
        assert(strRef.get() == null);
    }
    private static void testImmortalInternedString() {
        WeakReference strRef = new WeakReference<String>(CONST.intern());
        System.gc();
        assert(CONST == CONST.intern());
        assert(strRef.get() != null);
        String s = CONST;
        strRef = new WeakReference<String>(s.intern());
        s = "";
        System.gc();
        assert(strRef.get() == CONST);
    }
}
