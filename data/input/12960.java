class MonitorTestTarg {
    public static Object endingMonitor;
    public static Object startingMonitor;
    public static final long timeout = 30 * 6000; 
    public static volatile boolean aboutEnterLock;
    static void foo() {
        System.out.println("Howdy!");
    }
    public static void main(String[] args){
        endingMonitor = new Object();
        startingMonitor = new Object();
        myThread t1 = new myThread();
        foo();
        aboutEnterLock = false;
        synchronized(endingMonitor) {
            try {
                synchronized (startingMonitor) {
                    t1.start();
                    startingMonitor.wait(timeout);
                }
            } catch (InterruptedException e) {
                System.out.println("Interrupted exception " + e);
            }
            Thread.yield();
            while(!(aboutEnterLock && t1.getState() == Thread.State.BLOCKED)) {
                try {
                    Thread.sleep(1000);
                }catch(Exception x){
                    System.out.println(x);
                }
            }
        }
        try {
            t1.join(timeout);
        } catch (Exception x){
            System.out.println("Exception while thread.join :" + x);
        }
        System.out.println("Test exiting");
    }
}
class myThread extends Thread {
    public void run() {
        synchronized(MonitorTestTarg.startingMonitor) {
            MonitorTestTarg.startingMonitor.notify();
        }
        MonitorTestTarg.aboutEnterLock = true;
        synchronized (MonitorTestTarg.endingMonitor) {
        }
    }
}
public class MonitorEventTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    List monitors;
    MonitorContendedEnterRequest contendedEnterRequest;
    MonitorWaitedRequest monitorWaitedRequest;
    MonitorContendedEnteredRequest contendedEnteredRequest;
    MonitorWaitRequest monitorWaitRequest;
    static int actualWaitCount = 0;
    static int actualWaitedCount = 0;
    static int actualContendedEnterCount = 0;
    static int actualContendedEnteredCount= 0;
    MonitorEventTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new MonitorEventTest(args).startTests();
    }
    public void monitorContendedEnter(MonitorContendedEnterEvent event) {
        actualContendedEnterCount++;
    }
    public void monitorContendedEntered(MonitorContendedEnteredEvent event) {
        actualContendedEnteredCount++;
    }
    public void monitorWait(MonitorWaitEvent event) {
        actualWaitCount++;
    }
    public void monitorWaited(MonitorWaitedEvent event) {
        actualWaitedCount++;
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("MonitorTestTarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        int initialSize = mainThread.frames().size();
        if (vm().canRequestMonitorEvents()) {
            contendedEnterRequest = eventRequestManager().createMonitorContendedEnterRequest();
            contendedEnterRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE);
            contendedEnterRequest.enable();
            contendedEnteredRequest = eventRequestManager().createMonitorContendedEnteredRequest();
            contendedEnteredRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE);
            contendedEnteredRequest.enable();
            monitorWaitRequest = eventRequestManager().createMonitorWaitRequest();
            monitorWaitRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE);
            monitorWaitRequest.enable();
            monitorWaitedRequest = eventRequestManager().createMonitorWaitedRequest();
            monitorWaitedRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE);
            monitorWaitedRequest.enable();
        } else {
            System.out.println("request monitor events not supported " );
        }
        resumeTo("MonitorTestTarg", "foo", "()V");
        listenUntilVMDisconnect();
        if (vm().canRequestMonitorEvents()) {
            if (actualContendedEnterCount == 0) {
                failure("Did not receive any  contended enter event.");
            }
            if (actualContendedEnteredCount == 0) {
                failure("Did not receive any contended entered event. ");
            }
            if (actualWaitCount == 0) {
                failure("Did not receive any contended monitor wait event");
            }
            if (actualWaitedCount == 0) {
                failure("Did not receive any contended monitor waited event");
            }
        }
        if (!testFailed) {
            println("MonitorEventTest: passed");
        } else {
            throw new Exception("MonitorEventTest: failed");
        }
    }
}
