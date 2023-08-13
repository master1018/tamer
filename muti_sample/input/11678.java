public class Mtrace {
    private static int engaged = 0;
    private static native void _method_entry(Object thr, int cnum, int mnum);
    public static void method_entry(int cnum, int mnum)
    {
        if ( engaged != 0 ) {
            _method_entry(Thread.currentThread(), cnum, mnum);
        }
    }
    private static native void _method_exit(Object thr, int cnum, int mnum);
    public static void method_exit(int cnum, int mnum)
    {
        if ( engaged != 0 ) {
            _method_exit(Thread.currentThread(), cnum, mnum);
        }
    }
}
