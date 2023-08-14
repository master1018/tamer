class TemplateTarg {
    public static void main(String[] args){
        System.out.println("Howdy!");
        System.out.println("Goodbye from TemplateTarg!");
    }
}
public class TemplateTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    TemplateTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new TemplateTest(args).startTests();
    }
    public void eventSetReceived(EventSet set) {
        println("Got event set");
    }
    public void eventReceived(Event event) {
        println("Got event");
    }
    public void breakpointReached(BreakpointEvent event) {
        println("Got BreakpointEvent");
    }
    public void exceptionThrown(ExceptionEvent event) {
        println("Got ExceptionEvent");
    }
    public void stepCompleted(StepEvent event) {
        println("Got StepEvent");
    }
    public void classPrepared(ClassPrepareEvent event) {
        println("Got ClassPrepareEvent");
    }
    public void classUnloaded(ClassUnloadEvent event) {
        println("Got ClassUnloadEvent");
    }
    public void methodEntered(MethodEntryEvent event) {
        println("Got MethodEntryEvent");
    }
    public void methodExited(MethodExitEvent event) {
        println("Got MethodExitEvent");
    }
    public void fieldAccessed(AccessWatchpointEvent event) {
        println("Got AccessWatchpointEvent");
    }
    public void fieldModified(ModificationWatchpointEvent event) {
        println("Got ModificationWatchpointEvent");
    }
    public void threadStarted(ThreadStartEvent event) {
        println("Got ThreadStartEvent");
    }
    public void threadDied(ThreadDeathEvent event) {
        println("Got ThreadDeathEvent");
    }
    public void vmStarted(VMStartEvent event) {
        println("Got VMStartEvent");
    }
    public void vmDied(VMDeathEvent event) {
        println("Got VMDeathEvent");
    }
    public void vmDisconnected(VMDisconnectEvent event) {
        println("Got VMDisconnectEvent");
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("TemplateTarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        EventRequestManager erm = vm().eventRequestManager();
        StepRequest request = erm.createStepRequest(mainThread,
                                                    StepRequest.STEP_LINE,
                                                    StepRequest.STEP_OVER);
        request.enable();
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("TemplateTest: passed");
        } else {
            throw new Exception("TemplateTest: failed");
        }
    }
}
