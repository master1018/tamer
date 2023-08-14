public class PushPopTest {
    public static Frame frame;
    public static void main(String[] args) {
        frame = new Frame("");
        frame.pack();
        Runnable dummy = new Runnable() {
                public void run() {
                    System.err.println("Dummy is here.");
                    System.err.flush();
                }
            };
        EventQueue seq = Toolkit.getDefaultToolkit().getSystemEventQueue();
        MyEventQueue1 eq1 = new MyEventQueue1();
        MyEventQueue2 eq2 = new MyEventQueue2();
        EventQueue.invokeLater(dummy);
        seq.push(eq1);
        EventQueue.invokeLater(dummy);
        eq1.push(eq2);
        EventQueue.invokeLater(dummy);
        Runnable runnable = new Runnable() {
                public void run() {
                    System.err.println("Dummy from SunToolkit");
                    System.err.flush();
                }
            };
        InvocationEvent ie = new InvocationEvent(eq2, runnable, null, false);
        SunToolkit.postEvent(SunToolkit.targetToAppContext(frame), ie);
        eq1.pop();
        frame.dispose();
    }
}
class MyEventQueue1 extends EventQueue {
    public void pop() {
        super.pop();
    }
}
class MyEventQueue2 extends EventQueue {
    protected void pop() {
        System.err.println("pop2()");
        Thread.dumpStack();
        try {
            EventQueue.invokeAndWait(new Runnable() {
                    public void run() {
                        Runnable runnable = new Runnable() {
                                public void run() {
                                    System.err.println("Dummy from pop");
                                    System.err.flush();
                                }
                             };
                        InvocationEvent ie = new InvocationEvent(MyEventQueue2.this, runnable, null, false);
                        SunToolkit.postEvent(SunToolkit.targetToAppContext(PushPopTest.frame), ie);
                        postEvent(ie);
                    }
                });
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (java.lang.reflect.InvocationTargetException ie) {
            ie.printStackTrace();
        }
        super.pop();
    }
}
