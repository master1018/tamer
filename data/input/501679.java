public class T_invokestatic_12 implements Runnable {
    public final static int CNT = 1000;
    static int value = 0;
    static boolean failed = false;
    public void run() {
        for(int i = 0; i < CNT; i++) {
            test();
        }
    }
    private synchronized static void test()    {
        value++;
        int c = value;
        Thread.yield();
        if(c != value)
            failed = true;
    }
    public static boolean execute() {
        T_invokestatic_12 test = new T_invokestatic_12();
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
        if(value != CNT * 2)
            return false;
        return !failed;
    }
}