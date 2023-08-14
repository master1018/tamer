class SuspendThreadTarg {
    public static long count;
    public static void bkpt() {
        count++;
    }
    public static void main(String[] args){
        System.out.println("Howdy!");
        while(count >= 0) {
            bkpt();
        }
        System.out.println("Goodbye from SuspendThreadTarg, count = " + count);
    }
}
public class SuspendThreadTest extends TestScaffold {
    ClassType targetClass;
    ThreadReference mainThread;
    SuspendThreadTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new SuspendThreadTest(args).startTests();
    }
    static int maxBkpts = 200;
    int bkptCount;
    BreakpointRequest bkptRequest;
    Field debuggeeCountField;
    public void breakpointReached(BreakpointEvent event) {
        System.out.println("Got BreakpointEvent: " + bkptCount +
                           ", debuggeeCount = " +
                           ((LongValue)targetClass.
                            getValue(debuggeeCountField)).value()
                           );
        bkptRequest.disable();
    }
    public void eventSetComplete(EventSet set) {
        set.resume();
        if (bkptCount++ < maxBkpts) {
            bkptRequest.enable();
        }
    }
    public void vmDisconnected(VMDisconnectEvent event) {
        println("Got VMDisconnectEvent");
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("SuspendThreadTarg");
        targetClass = (ClassType)bpe.location().declaringType();
        mainThread = bpe.thread();
        EventRequestManager erm = vm().eventRequestManager();
        Location loc1 = findMethod(targetClass, "bkpt", "()V").location();
        bkptRequest = erm.createBreakpointRequest(loc1);
        bkptRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
        bkptRequest.enable();
        debuggeeCountField = targetClass.fieldByName("count");
        try {
            addListener (this);
        } catch (Exception ex){
            ex.printStackTrace();
            failure("failure: Could not add listener");
            throw new Exception("SuspendThreadTest: failed");
        }
        int prevBkptCount;
        vm().resume();
        while (bkptCount < maxBkpts) {
            prevBkptCount = bkptCount;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ee) {
            }
            if (prevBkptCount == bkptCount) {
                failure("failure: test hung");
                break;
            }
            prevBkptCount = bkptCount;
        }
        println("done with loop");
        bkptRequest.disable();
        removeListener(this);
        if (!testFailed) {
            println("SuspendThreadTest: passed");
        } else {
            throw new Exception("SuspendThreadTest: failed");
        }
    }
}
