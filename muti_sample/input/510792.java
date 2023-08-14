public class T_monitorenter_1 {
    public int counter = 0;
    public void run() throws InterruptedException  {
        synchronized(this) {
            int a = counter;
            Thread.sleep(500);
            counter = ++a;
        }
    }
}
