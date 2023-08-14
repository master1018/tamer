public class HeapTracker {
    private static int engaged = 0;
    private static native void _newobj(Object thread, Object o);
    public static void newobj(Object o)
    {
        if ( engaged != 0 ) {
            _newobj(Thread.currentThread(), o);
        }
    }
    private static native void _newarr(Object thread, Object a);
    public static void newarr(Object a)
    {
        if ( engaged != 0 ) {
            _newarr(Thread.currentThread(), a);
        }
    }
}
