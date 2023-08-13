class DeleteEventRequestsTarg {
    public static void main(String[] args){
        System.out.println("Howdy!");
        System.out.println("Goodbye from DeleteEventRequestsTarg!");
    }
}
public class DeleteEventRequestsTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    DeleteEventRequestsTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new DeleteEventRequestsTest(args).startTests();
    }
    public void stepCompleted(StepEvent event) {
        failure("Got StepEvent which was deleted");
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("DeleteEventRequestsTarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        EventRequestManager erm = vm().eventRequestManager();
        StepRequest request = erm.createStepRequest(mainThread,
                                                    StepRequest.STEP_LINE,
                                                    StepRequest.STEP_OVER);
        request.enable();
        erm.deleteEventRequests(erm.stepRequests());
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("DeleteEventRequestsTest: passed");
        } else {
            throw new Exception("DeleteEventRequestsTest: failed");
        }
    }
}
