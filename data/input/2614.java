class ExpiredRequestDeletionTarg {
    int foo = 9;
    public static void main(String[] args){
        System.out.println("Why, hello there...");
        (new ExpiredRequestDeletionTarg()).bar();
    }
    void bar() {
        ++foo;
    }
}
public class ExpiredRequestDeletionTest extends TestScaffold {
    EventRequestManager erm;
    ReferenceType targetClass;
    ThreadReference mainThread;
    Throwable throwable = null;
    ExpiredRequestDeletionTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new ExpiredRequestDeletionTest(args).startTests();
    }
    public void breakpointReached(BreakpointEvent event) {
        try {
            EventRequest req = event.request();
            if (req != null) {
                println("Deleting BreakpointRequest");
                erm.deleteEventRequest(req);
            } else {
                println("Got BreakpointEvent with null request");
            }
        } catch (Throwable exc) {
            throwable = exc;
            failure("Deleting BreakpointRequest threw - " + exc);
        }
    }
    public void stepCompleted(StepEvent event) {
        try {
            EventRequest req = event.request();
            if (req != null) {
                println("Deleting StepRequest");
                erm.deleteEventRequest(req);
            } else {
                println("Got StepEvent with null request");
            }
        } catch (Throwable exc) {
            throwable = exc;
            failure("Deleting StepRequest threw - " + exc);
        }
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("ExpiredRequestDeletionTarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        erm = vm().eventRequestManager();
        List meths = targetClass.methodsByName("bar");
        if (meths.size() != 1) {
            throw new Exception("test error: should be one bar()");
        }
        Method barMethod = (Method)meths.get(0);
        StepRequest sr = erm.createStepRequest(mainThread,
                                                    StepRequest.STEP_LINE,
                                                    StepRequest.STEP_OVER);
        sr.addCountFilter(1);
        sr.enable();
        BreakpointRequest bpr =
            erm.createBreakpointRequest(barMethod.location());
        bpr.addCountFilter(1);
        bpr.enable();
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("ExpiredRequestDeletionTest: passed");
        } else {
            throw new Exception("ExpiredRequestDeletionTest: failed", throwable);
        }
    }
}
