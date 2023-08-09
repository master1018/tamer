class ResumeOneThreadTarg extends Thread {
    static String name1 = "Thread 1";
    static String name2 = "Thread 2";
    public ResumeOneThreadTarg(String name) {
        super(name);
    }
    public static void main(String[] args) {
        System.out.println("    Debuggee: Howdy!");
        ResumeOneThreadTarg t1 = new ResumeOneThreadTarg(name1);
        ResumeOneThreadTarg t2 = new ResumeOneThreadTarg(name2);
        t1.start();
        t2.start();
    }
    public void run() {
        if (getName().equals(name1)) {
            run1();
        } else {
            run2();
        }
    }
    public void bkpt1(String p1) {
        System.out.println("    Debuggee: bkpt 1");
    }
    public void run1() {
        bkpt1("Hello Alviso!");
    }
    public void bkpt2() {
        System.out.println("    Debuggee: bkpt 2");
    }
    public void run2() {
        bkpt2();
    }
}
public class ResumeOneThreadTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    BreakpointRequest request1;
    BreakpointRequest request2;
    ThreadReference thread1 = null;
    ThreadReference thread2 = null;;
    boolean theVMisDead = false;
    ResumeOneThreadTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new ResumeOneThreadTest(args).startTests();
    }
    synchronized public void breakpointReached(BreakpointEvent event) {
        println("-- Got bkpt at: " + event.location());
        ThreadReference eventThread = event.thread();
        if (eventThread.name().equals(ResumeOneThreadTarg.name1)) {
            thread1 = eventThread;
        }
        if (eventThread.name().equals(ResumeOneThreadTarg.name2)) {
            thread2 = eventThread;
        }
    }
    public void vmDied(VMDeathEvent event) {
        theVMisDead = true;
    }
    synchronized public void eventSetComplete(EventSet set) {
        if (theVMisDead) {
            return;
        }
        if (thread1 == null || thread2 == null) {
            return;
        }
        println("-- All threads suspended");
        vm().suspend();
        StackFrame t1sf0 = null;
        try {
            t1sf0 = thread1.frame(0);
        } catch (IncompatibleThreadStateException ee) {
            failure("FAILED: Exception: " + ee);
        }
        println("-- t1sf0 args: " + t1sf0.getArgumentValues());
        request2.disable();
        thread2.resume();
        thread2.resume();
        println("-- Did Resume on thread 2");
        try {
            StackFrame t1sf0_1 = thread1.frame(0);
            if (!t1sf0.equals(t1sf0_1)) {
                failure("FAILED: Got a different frame 0 for thread 1 after resuming thread 2");
            }
        } catch (IncompatibleThreadStateException ee) {
            failure("FAILED: Could not get frames for thread 1: Exception: " + ee);
        } catch (Exception ee) {
            failure("FAILED: Could not get frames for thread 1: Exception: " + ee);
        }
        try {
            println("-- t1sf0 args: " + t1sf0.getArgumentValues());
        } catch (InvalidStackFrameException ee) {
            failure("FAILED Got InvalidStackFrameException");
            vm().dispose();
            throw(ee);
        }
        request1.disable();
        thread1.resume();
        vm().resume();
        println("--------------");
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("ResumeOneThreadTarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        EventRequestManager erm = vm().eventRequestManager();
        final Thread mainThread = Thread.currentThread();
        Location loc1 = findMethod(targetClass, "bkpt1", "(Ljava/lang/String;)V").location();
        request1 = erm.createBreakpointRequest(loc1);
        request1.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
        request1.enable();
        Location loc2 = findMethod(targetClass, "bkpt2", "()V").location();
        request2 = erm.createBreakpointRequest(loc2);
        request2.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
        request2.enable();
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("ResumeOneThreadTest: passed");
        } else {
            throw new Exception("ResumeOneThreadTest: failed");
        }
    }
}
