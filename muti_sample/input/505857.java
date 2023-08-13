public class T_monitor_enter_1 {
    public int counter = 0;
    public void run() throws InterruptedException  {
        synchronized(this) {
            int a = counter;
            Thread.sleep(500);
            counter = ++a;
        }
    }
}
