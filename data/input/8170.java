class SimulResumerTarg extends Thread {
    static boolean one = false;
    static String name1 = "Thread 1";
    static String name2 = "Thread 2";
    static int count = 10000;
    public static void main(String[] args) {
        System.out.println("Howdy!");
        SimulResumerTarg t1 = new SimulResumerTarg(name1);
        SimulResumerTarg t2 = new SimulResumerTarg(name2);
        t1.start();
        t2.start();
    }
    public SimulResumerTarg(String name) {
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
        synchronized(name1) {
            yield();
        }
    }
    public void run1() {
        int i = 0;
        while (i < count) {
            i++;
            bkpt1(i);
        }
    }
    public void bkpt2(int i) {
        synchronized(name2) {
            yield();
        }
    }
    public void run2() {
        int i = 0;
        while (i < count) {
            i++;
            bkpt2(i);
        }
    }
}
public class SimulResumerTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    BreakpointRequest request1;
    BreakpointRequest request2;
    static volatile int bkpts = 0;
    static int iters = 0;
    Thread resumerThread;
    static int waitTime = 100;
    ThreadReference debuggeeThread1 = null;
    ThreadReference debuggeeThread2 = null;
    SimulResumerTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new SimulResumerTest(args).startTests();
    }
    public void breakpointReached(BreakpointEvent event) {
        ThreadReference thr = event.thread();
        if (bkpts == 0) {
            resumerThread.start();
            debuggeeThread1 = thr;
            System.out.println("thr1 = " + debuggeeThread1);
        }
        if (debuggeeThread2 == null && thr != debuggeeThread1) {
            debuggeeThread2 = thr;
            System.out.println("thr2 = " + debuggeeThread2);
        }
        synchronized("abc") {
            bkpts++;
        }
    }
    void check(ThreadReference thr) {
        String kind = "";
        if (thr != null) {
            try {
                kind = "ownedMonitors()";
                System.out.println("kind = " + kind);
                if (thr.ownedMonitors() == null) {
                    failure("failure: ownedMonitors = null");
                }
                kind = "ownedMonitorsAndFrames()";
                System.out.println("kind = " + kind);
                if (thr.ownedMonitorsAndFrames() == null) {
                    failure("failure: ownedMonitorsAndFrames = null");
                }
                kind = "currentContendedMonitor()";
                System.out.println("kind = " + kind);
                thr.currentContendedMonitor();
                kind = "frames()";
                System.out.println("kind = " + kind);
                List<StackFrame> frames = thr.frames();
                int nframes = frames.size();
                if (nframes > 0) {
                    kind = "frames(0, size - 1)";
                System.out.println("kind = " + kind);
                    thr.frames(0, frames.size() - 1);
                }
                kind = "frameCount()";
                System.out.println("kind = " + kind);
                if (thr.frameCount() == -1) {
                    failure("failure: frameCount = -1");
                }
                kind = "name()";
                System.out.println("kind = " + kind);
                if (thr.name() == null) {
                    failure("failure: name = null");
                }
                kind = "status()";
                System.out.println("kind = " + kind);
                if (thr.status() < 0) {
                    failure("failure: status < 0");
                }
            } catch (IncompatibleThreadStateException ee) {
            } catch (VMDisconnectedException ee) {
                throw ee;
            } catch (Exception ee) {
                failure("failure: Got exception from " + kind + ": " + ee );
            }
        }
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("SimulResumerTarg");
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
        resumerThread = new Thread("test resumer") {
                public void run() {
                    while (true) {
                        iters++;
                        System.out.println("bkpts = " + bkpts + ", iters = " + iters);
                        try {
                            Thread.sleep(waitTime);
                            check(debuggeeThread1);
                            check(debuggeeThread2);
                        } catch (InterruptedException ee) {
                            println("resumer Interrupted");
                            break;
                        } catch (VMDisconnectedException ee) {
                            println("VMDisconnectedException");
                            break;
                        }
                    }
                }
            };
        listenUntilVMDisconnect();
        resumerThread.interrupt();
        if (!testFailed) {
            println("SimulResumerTest: passed; bkpts = " + bkpts + ", iters = " + iters);
        } else {
            throw new Exception("SimulResumerTest: failed; bkpts = " + bkpts + ", iters = " + iters);
        }
    }
}
