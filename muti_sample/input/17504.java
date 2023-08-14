class AfterDeathTarg {
    public static void main(String[] args){
        System.out.println("Howdy!");
        System.out.println("Goodbye from AfterDeathTarg!");
    }
}
public class AfterThreadDeathTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    StepRequest stepRequest = null;
    EventRequestManager erm;
    boolean mainIsDead;
    AfterThreadDeathTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new AfterThreadDeathTest(args).startTests();
    }
    public void threadStarted(ThreadStartEvent event) {
        println("Got ThreadStartEvent: " + event);
        if (stepRequest != null) {
            erm.deleteEventRequest(stepRequest);
            stepRequest = null;
            println("Deleted stepRequest");
        }
        if (mainIsDead) {
            stepRequest = erm.createStepRequest(mainThread,
                                                StepRequest.STEP_LINE,
                                                StepRequest.STEP_OVER);
            stepRequest.addCountFilter(1);
            stepRequest.setSuspendPolicy (EventRequest.SUSPEND_ALL);
            try {
                stepRequest.enable();
            } catch (IllegalThreadStateException ee) {
                println("Ok; got expected IllegalThreadStateException");
                return;
            } catch (Exception ee) {
                failure("FAILED: Did not get expected"
                      + " IllegalThreadStateException"
                      + " on a StepRequest.enable().  \n"
                      + "        Got this exception instead: " + ee);
                return;
            }
            failure("FAILED: Did not get expected IllegalThreadStateException"
                    + " on a StepRequest.enable()");
        }
    }
    public void threadDied(ThreadDeathEvent event) {
        println("Got ThreadDeathEvent: " + event);
        if (! mainIsDead) {
            if (mainThread.equals(event.thread())) {
                mainIsDead = true;
            }
        }
    }
    public void vmDied(VMDeathEvent event) {
        println("Got VMDeathEvent");
    }
    public void vmDisconnected(VMDisconnectEvent event) {
        println("Got VMDisconnectEvent");
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("AfterDeathTarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        erm = vm().eventRequestManager();
        ThreadStartRequest request = erm.createThreadStartRequest();
        request.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        request.enable();
        ThreadDeathRequest request1 = erm.createThreadDeathRequest();
        request1.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        request1.enable();
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("AfterThreadDeathTest: passed");
        } else {
            throw new Exception("AfterThreadDeathTest: failed");
        }
    }
}
