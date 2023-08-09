public class Tracker {
    private static int engaged = 0;
    private static native void nativeObjectInit(Object thr, Object obj);
    public static void ObjectInit(Object obj)
    {
        if ( engaged != 0 ) {
            nativeObjectInit(Thread.currentThread(), obj);
        }
    }
    private static native void nativeNewArray(Object thr, Object obj);
    public static void NewArray(Object obj)
    {
        if ( engaged != 0 ) {
            nativeNewArray(Thread.currentThread(), obj);
        }
    }
    private static native void nativeCallSite(Object thr, int cnum, int mnum);
    public static void CallSite(int cnum, int mnum)
    {
        if ( engaged != 0 ) {
            nativeCallSite(Thread.currentThread(), cnum, mnum);
        }
    }
    private static native void nativeReturnSite(Object thr, int cnum, int mnum);
    public static void ReturnSite(int cnum, int mnum)
    {
        if ( engaged != 0 ) {
            nativeReturnSite(Thread.currentThread(), cnum, mnum);
        }
    }
}
