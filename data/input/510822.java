public class T_areturn_7 implements Runnable {
    public final static int CNT = 1000;
    Integer value = new Integer(0);
    boolean failed = false;
    public void run() {
        for(int i = 0; i < CNT; i++) {
            test();
        }
    }
    private synchronized Integer test()  {
        Integer c = new Integer(value.intValue() + 1);
        value = c;
        Thread.yield();
        if(c != value)
            failed = true;
        return c;
    }
    public static boolean execute() {
        T_areturn_7 test = new T_areturn_7();
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
        if(test.value.intValue() != CNT * 2)
            return false;
        return !test.failed;
    }
}
