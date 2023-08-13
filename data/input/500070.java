public class T_invokespecial_22 implements Runnable {
    public final static int CNT = 1000;
    int value = 0;
    boolean failed = false;
    public void run() {
        for(int i = 0; i < CNT; i++) {
            test();
        }
    }
    private synchronized void test()    {
        value++;
        int c = value;
        Thread.yield();
        if(c != value)
            failed = true;
    }
    public static boolean execute() {
        T_invokespecial_22 test = new T_invokespecial_22();
        Thread t1 = new Thread(test);
        Thread t2 = new Thread(test);
        t1.start();
        t2.start();
        try
        {
            Thread.sleep(5000);
        }
        catch(InterruptedException ie) {
            return false;
        }
        if(test.value != CNT * 2)
            return false;
        return !test.failed;
    }
}