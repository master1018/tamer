class BreakpointTarg {
    public final static int BKPT_LINE = 54;
    public static long count;
    static void doit() {
        Object[] roots = new Object[200000];
        while (true) {
            int index = (int) (Math.random() * roots.length); 
            roots[index] = new Object();   
            count++;
        }
    }
    public static void main(String[] args) {
        doit();
    }
}
public class BreakpointTest extends TestScaffold {
    ClassType targetClass;
    ThreadReference mainThread;
    BreakpointTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new BreakpointTest(args).startTests();
    }
    static int maxBkpts = 50;
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
            try {
                Thread.sleep(100);
            } catch (InterruptedException ee) {
            }
            bkptRequest.enable();
        }
    }
    public void vmDisconnected(VMDisconnectEvent event) {
        println("Got VMDisconnectEvent");
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("BreakpointTarg");
        targetClass = (ClassType)bpe.location().declaringType();
        mainThread = bpe.thread();
        EventRequestManager erm = vm().eventRequestManager();
        Location loc1 = findLocation(
                            targetClass,
                            BreakpointTarg.BKPT_LINE);
        bkptRequest = erm.createBreakpointRequest(loc1);
        bkptRequest.enable();
        debuggeeCountField = targetClass.fieldByName("count");
        try {
            addListener (this);
        } catch (Exception ex){
            ex.printStackTrace();
            failure("failure: Could not add listener");
            throw new Exception("BreakpointTest: failed");
        }
        int prevBkptCount;
        vm().resume();
        while (!vmDisconnected && bkptCount < maxBkpts) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ee) {
            }
        }
        println("done with loop, final count = " +
                    ((LongValue)targetClass.
                     getValue(debuggeeCountField)).value());
        bkptRequest.disable();
        removeListener(this);
        if (!testFailed) {
            println("BreakpointTest: passed");
        } else {
            throw new Exception("BreakpointTest: failed");
        }
    }
}
