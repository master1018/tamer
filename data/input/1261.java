class PopAndInvokeTarg {
    static boolean waiting = false;
    public static void A() {
        System.out.println("    debuggee: in A");
    }
    public static void invokeee() {
        System.out.println("    debuggee: invokee");
    }
    public static void waiter() {
        if (waiting) {
            return;
        }
        waiting = true;
        System.out.println("    debuggee: in waiter");
        while (true) {
        }
    }
    public static void main(String[] args) {
        System.out.println("    debuggee: Howdy!");
        A();
        waiter();
        System.out.println("    debuggee: Goodbye from PopAndInvokeTarg!");
    }
}
public class PopAndInvokeTest extends TestScaffold {
    ClassType targetClass;
    ThreadReference mainThread;
    PopAndInvokeTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new PopAndInvokeTest(args).startTests();
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
    protected void runTests() throws Exception {
        runOnce();
    }
    void runOnce() throws Exception {
        BreakpointEvent bpe = startTo("PopAndInvokeTarg", "A", "()V");
        targetClass = (ClassType)bpe.location().declaringType();
        mainThread = bpe.thread();
        mainThread.popFrames(frameFor("A"));
        System.out.println("Debugger: Popped back to the call to A()");
        System.out.println("Debugger: Doing invoke");
        Method invokeeeMethod = (Method)targetClass.methodsByName("invokeee").get(0);
        try {
            targetClass.invokeMethod(mainThread, invokeeeMethod,
                                     new ArrayList(), 0);
        } catch (Exception ex) {
            failure("failure: invoke got unexpected exception: " + ex);
            ex.printStackTrace();
        }
        System.out.println("Debugger: invoke done");
        System.out.println("Debugger: Resuming debuggee");
        vm().resume();
        Field waiting = targetClass.fieldByName("waiting");
        while (true) {
            BooleanValue bv= (BooleanValue)targetClass.getValue(waiting);
            if (!bv.value()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ee) {
                }
                continue;
            }
            System.out.println("Debugger: Suspending debuggee");
            vm().suspend();
            System.out.println("Debugger: Popping frame for waiter");
            mainThread.popFrames(frameFor("waiter"));
            System.out.println("Debugger: Invoking method");
            try {
                targetClass.invokeMethod(mainThread, invokeeeMethod,
                                         new ArrayList(), 0);
            } catch (IncompatibleThreadStateException ee) {
                System.out.println("Debugger: Success: Got expected IncompatibleThreadStateException");
                break;
            } catch (Exception ee) {
                failure("FAIL: Got unexpected exception: " + ee);
                break;
            }
            failure("FAIL: Did not get IncompatibleThreadStateException " +
                    "when debuggee is not suspended by an event");
        }
        listenUntilVMDisconnect();
        if (testFailed) {
            throw new Exception("PopAndInvokeTest failed");
        }
        System.out.println("Passed:");
    }
}
