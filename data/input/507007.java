public class T_invokeinterface_19 implements Runnable, ITest {
    public final static int CNT = 1000;
    int value = 0;
    boolean failed = false;
    public void run() {
        for(int i = 0; i < CNT; i++) {
            doit();
        }
    }
    public synchronized void doit(){
        value++;
        int c = value;
        Thread.yield();
        if(c != value)
            failed = true;
    }
    public void doit(int i){
    }
    public void doitNative(){
    }
    public  int test(int a) {
        return 0;
    }
    public int testArgsOrder(int a, int b){
        return 0;
    }
    public static boolean execute() {
        T_invokeinterface_19 test = new T_invokeinterface_19();
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