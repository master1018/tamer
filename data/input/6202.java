public class InvocationEventTest
{
    public static void main(String []s) throws Exception
    {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Runnable runnable = new Runnable() {
            public void run() {
            }
        };
        Object lock = new Object();
        InvocationEvent event = new InvocationEvent(tk, runnable, lock, true);
        if (event.isDispatched()) {
            throw new RuntimeException(" Initially, the event shouldn't be dispatched ");
        }
        synchronized(lock) {
            tk.getSystemEventQueue().postEvent(event);
            while(!event.isDispatched()) {
                lock.wait();
            }
        }
        if(!event.isDispatched()) {
            throw new RuntimeException(" Finally, the event should be dispatched ");
        }
    }
}
