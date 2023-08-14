public class TraceTest extends AndroidTestCase
{
    private static final String TAG = "TraceTest";
    private int eMethodCalls = 0;
    private int fMethodCalls = 0;
    private int gMethodCalls = 0;
    @SmallTest
    public void testNativeTracingFromJava()
    {
        long start = System.currentTimeMillis();
        Debug.startNativeTracing();
        int count = 0;
        for (int ii = 0; ii < 20; ii++) {
            count = eMethod();
        }
        Debug.stopNativeTracing();
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        Log.i(TAG, "elapsed millis: " + elapsed);
        Log.i(TAG, "eMethod calls: " + eMethodCalls
                + " fMethod calls: " + fMethodCalls
                + " gMethod calls: " + gMethodCalls);
    }
    @Suppress
    public void disableTestNativeTracingFromC()
    {
        long start = System.currentTimeMillis();
        nativeMethodAndStartTracing();
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        Log.i(TAG, "elapsed millis: " + elapsed);
    }
    native void nativeMethod();
    native void nativeMethodAndStartTracing();
    @LargeTest
    public void testMethodTracing()
    {
        long start = System.currentTimeMillis();
        Debug.startMethodTracing("traceTest");
        topMethod();
        Debug.stopMethodTracing();
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        Log.i(TAG, "elapsed millis: " + elapsed);
    }
    private void topMethod() {
        aMethod();
        bMethod();
        cMethod();
        dMethod(5);
        Thread t1 = new aThread();
        t1.start();
        Thread t2 = new aThread();
        t2.start();
        Thread t3 = new aThread();
        t3.start();
        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
        }
    }
    private class aThread extends Thread {
        @Override
        public void run() {
            aMethod();
            bMethod();
            cMethod();
        }
    }
    private int aMethod() {
        int count = 0;
        for (int ii = 0; ii < 6; ii++) {
            count += bMethod();
        }
        for (int ii = 0; ii < 5; ii++) {
            count += cMethod();
        }
        for (int ii = 0; ii < 4; ii++) {
            count += dMethod(ii);
        }
        return count;
    }
    private int bMethod() {
        int count = 0;
        for (int ii = 0; ii < 4; ii++) {
            count += cMethod();
        }
        return count;
    }
    private int cMethod() {
        int count = 0;
        for (int ii = 0; ii < 1000; ii++) {
            count += ii;
        }
        return count;
    }
    private int dMethod(int level) {
        int count = 0;
        if (level > 0) {
            count = dMethod(level - 1);
        }
        for (int ii = 0; ii < 100; ii++) {
            count += ii;
        }
        if (level == 0) {
            return count;
        }
        return dMethod(level - 1);
    }
    public int eMethod() {
        eMethodCalls += 1;
        int count = fMethod();
        count += gMethod(3);
        return count;
    }
    public int fMethod() {
        fMethodCalls += 1;
        int count = 0;
        for (int ii = 0; ii < 10; ii++) {
            count += ii;
        }
        return count;
    }
    public int gMethod(int level) {
        gMethodCalls += 1;
        int count = level;
        if (level > 1)
            count += gMethod(level - 1);
        return count;
    }
    static {
        Log.i(TAG, "Loading trace_test native library...");
        try {
            System.loadLibrary("trace_test");
            Log.i(TAG, "Successfully loaded trace_test native library");
        }
        catch (UnsatisfiedLinkError ule) {
            Log.w(TAG, "Could not load trace_test native library");
        }
    }
}
