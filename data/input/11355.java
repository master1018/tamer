class TwoThreadsTarg extends Thread {
    static boolean one = false;
    static String name1 = "Thread 1";
    static String name2 = "Thread 2";
    static int count = 100;
    public static void main(String[] args) {
        System.out.println("Howdy!");
        TwoThreadsTarg t1 = new TwoThreadsTarg(name1);
        TwoThreadsTarg t2 = new TwoThreadsTarg(name2);
        t1.start();
        t2.start();
    }
    public TwoThreadsTarg(String name) {
        super(name);
    }
    public void run() {
        if (getName().equals(name1)) {
            run1();
        } else {
            run2();
        }
    }
    public void bkpt1(int i) {
        yield();
    }
    public void run1() {
        int i = 0;
        while (i < count) {
            i++;
            bkpt1(i);
        }
    }
    public void bkpt2(int i) {
        yield();
    }
    public void run2() {
        int i = 0;
        while (i < count) {
            i++;
            bkpt2(i);
        }
    }
}
public class TwoThreadsTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    BreakpointRequest request1;
    BreakpointRequest request2;
    static volatile int bkpts = 0;
    Thread timerThread;
    static int waitTime = 20000;
    TwoThreadsTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new TwoThreadsTest(args).startTests();
    }
    public void breakpointReached(BreakpointEvent event) {
        if (bkpts == 0) {
            timerThread.start();
        }
        synchronized("abc") {
            bkpts++;
        }
        request1.disable();
        request2.disable();
        Method mmm = event.location().method();
        List lvlist;
        try {
            lvlist = mmm.variablesByName("i");
        } catch (AbsentInformationException ee) {
            failure("FAILED: can't get local var i");
            return;
        }
        LocalVariable ivar = (LocalVariable)lvlist.get(0);
        ThreadReference thr = event.thread();
        StackFrame sf;
        try {
            sf = thr.frame(0);
        } catch (IncompatibleThreadStateException ee) {
            failure("FAILED: bad thread state");
            return;
        }
        Value ival = sf.getValue(ivar);
        println("Got bkpt at: " + event.location() + ", i = " + ival);
        request1.enable();
        request2.enable();
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("TwoThreadsTarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        EventRequestManager erm = vm().eventRequestManager();
        final Thread mainThread = Thread.currentThread();
        Location loc1 = findMethod(targetClass, "bkpt1", "(I)V").location();
        Location loc2 = findMethod(targetClass, "bkpt2", "(I)V").location();
        request1 = erm.createBreakpointRequest(loc1);
        request2 = erm.createBreakpointRequest(loc2);
        request1.enable();
        request2.enable();
        timerThread = new Thread("test timer") {
                public void run() {
                    int myBkpts = bkpts;
                    while (true) {
                        try {
                            Thread.sleep(waitTime);
                            System.out.println("bkpts = " + bkpts);
                            if (myBkpts == bkpts) {
                                failure("failure: Debuggee appears to be hung");
                                vmDisconnected = true;
                                mainThread.interrupt();
                                break;
                            }
                            myBkpts = bkpts;
                        } catch (InterruptedException ee) {
                            println("timer Interrupted");
                            break;
                        }
                    }
                }
            };
        listenUntilVMDisconnect();
        timerThread.interrupt();
        if (!testFailed) {
            println("TwoThreadsTest: passed; bkpts = " + bkpts);
        } else {
            throw new Exception("TwoThreadsTest: failed; bkpts = " + bkpts);
        }
    }
}
