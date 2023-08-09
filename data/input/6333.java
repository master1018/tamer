class InvokeHangTarg extends Thread {
    static boolean one = false;
    static String name1 = "Thread 1";
    static String name2 = "Thread 2";
    static int count = 100;
    public static void main(String[] args) {
        System.out.println("Howdy!");
        InvokeHangTarg t1 = new InvokeHangTarg(name1);
        InvokeHangTarg t2 = new InvokeHangTarg(name2);
        t1.start();
        t2.start();
    }
    public double invokeee() {
        System.out.println("Debuggee: invokeee in thread "+Thread.currentThread().toString());
        yield();
        return longMethod(2);
    }
    public double longMethod(int n) {
        double a = 0;
        double s = 0;
        for (int i = 0; i < n; i++) {
            a += i;
            for (int j = -1000*i; j < 1000*i; j++) {
                a = a*(1 + i/(j + 0.5));
                s += Math.sin(a);
            }
        }
        System.out.println("Debuggee: invokeee finished");
        return s;
    }
    public InvokeHangTarg(String name) {
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
        System.out.println("Debuggee: " + Thread.currentThread() +" is running:" + i);
        try {
            Thread.currentThread().sleep(2);
        } catch (InterruptedException iex) {}
    }
    public void run1() {
        int i = 0;
        while (i < count) {
            i++;
            bkpt1(i);
        }
    }
    public void bkpt2(int i) {
        System.out.println("Debuggee: " + Thread.currentThread() +" is running:" + i);
        try {
            Thread.currentThread().sleep(2);
        } catch (InterruptedException iex) {}
    }
    public void run2() {
        int i = 0;
        while (i < count) {
            i++;
            bkpt2(i);
        }
    }
}
public class InvokeHangTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    BreakpointRequest request1;
    BreakpointRequest request2;
    static volatile int bkpts = 0;
    Thread timerThread;
    static int waitTime = 20000;
    InvokeHangTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new InvokeHangTest(args).startTests();
    }
    void doInvoke(ThreadReference thread, ObjectReference ref, String methodName) {
        List methods = ref.referenceType().methodsByName(methodName);
        Method method = (Method) methods.get(0);
        try {
            System.err.println("  Debugger: Invoking in thread" + thread);
            ref.invokeMethod(thread, method, new ArrayList(), ref.INVOKE_NONVIRTUAL);
            System.err.println("  Debugger: Invoke done");
        } catch (Exception ex) {
            ex.printStackTrace();
            failure("failure: Exception");
        }
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
        ThreadReference thread = event.thread();
        try {
            StackFrame sf = thread.frame(0);
            System.err.println("  Debugger: Breakpoint hit at "+sf.location());
            doInvoke(thread, sf.thisObject(), "invokeee");
        } catch (IncompatibleThreadStateException itsex) {
            itsex.printStackTrace();
            failure("failure: Exception");
        }
        request1.enable();
        request2.enable();
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("InvokeHangTarg");
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
            println("InvokeHangTest: passed; bkpts = " + bkpts);
        } else {
            throw new Exception("InvokeHangTest: failed; bkpts = " + bkpts);
        }
    }
}
