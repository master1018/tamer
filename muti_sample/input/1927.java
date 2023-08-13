class PopAndStepTarg {
    public void B() {
        System.out.println("debuggee: in B");
        System.out.println("debuggee: in B, back to A");   
    }
    public void A() {
        System.out.println("debuggee: in A, about to call B");  
        B();
        System.out.println("debuggee: in A, back from B");      
        throw new RuntimeException("debuggee: Got to line 33");
    }
    public static void main(String[] args) {
        System.out.println("debuggee: Howdy!");      
        PopAndStepTarg xxx = new PopAndStepTarg();   
        xxx.A();                                     
        System.out.println("debugee: Goodbye from PopAndStepTarg!");
    }
}
public class PopAndStepTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    PopAndStepTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new PopAndStepTest(args).startTests();
    }
    StackFrame frameFor(String methodName) throws Exception {
        Iterator it = mainThread.frames().iterator();
        while (it.hasNext()) {
            StackFrame frame = (StackFrame)it.next();
            if (frame.location().method().name().equals(methodName)) {
                return frame;
            }
        }
        failure("FAIL: " + methodName + " not on stack");
        return null;
    }
    int getDebuggeeLineNum(int expectedLine) throws Exception {
        List allFrames = mainThread.frames();
        if ( allFrames == null) {
            return -1;
        }
        Iterator it = allFrames.iterator();
        StackFrame frame = (StackFrame)it.next();
        Location loc = frame.location();
        int theLine = loc.lineNumber();
        if (expectedLine != theLine) {
            failure("FAIL: Should be at " + expectedLine + ", are at " +
                    theLine + ", method = " + loc.method().name());
        } else {
            println("Should be at, and am at: " + expectedLine);
        }
        return theLine;
    }
    public void vmDied(VMDeathEvent event) {
        println("Got VMDeathEvent");
    }
    public void vmDisconnected(VMDisconnectEvent event) {
        println("Got VMDisconnectEvent");
    }
    protected void runTests() throws Exception {
        runOnce();
    }
    void runOnce() throws Exception{
        BreakpointEvent bpe = startToMain("PopAndStepTarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        getDebuggeeLineNum(37);
        println("Resuming to line 26");
        bpe = resumeTo("PopAndStepTarg", 26); getDebuggeeLineNum(26);
        EventRequestManager erm = eventRequestManager();
        StepRequest srInto = erm.createStepRequest(mainThread, StepRequest.STEP_LINE,
                                                   StepRequest.STEP_INTO);
        srInto.addClassExclusionFilter("java.*");
        srInto.addClassExclusionFilter("sun.*");
        srInto.addClassExclusionFilter("com.sun.*");
        srInto.addCountFilter(1);
        srInto.enable(); 
        mainThread.popFrames(frameFor("A"));
        println("Popped back to line 40 in main, the call to A()");
        println("Stepping into line 30");
        waitForRequestedEvent(srInto);   
        srInto.disable();
        getDebuggeeLineNum(30);
        println("Stepping over to line 31");
        stepOverLine(mainThread);   
        getDebuggeeLineNum(31);
        println("Stepping over to line 32");
        stepOverLine(mainThread);        
        getDebuggeeLineNum(32);
        vm().exit(0);
        if (testFailed) {
            throw new Exception("PopAndStepTest failed");
        }
        println("Passed:");
    }
}
