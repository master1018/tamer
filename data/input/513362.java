public class T_lreturn_7 implements Runnable {
    public final static int CNT = 1000;
    long value = 0;
    boolean failed = false;
    public void run() {
        for(int i = 0; i < CNT; i++) {
            test();
        }
    }
    private synchronized long test()  {
        value++;
        long c = value;
        Thread.yield();
        if(c != value)
            failed = true;
        return c;
    }
    public static boolean execute() {
        T_lreturn_7 test = new T_lreturn_7();
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
