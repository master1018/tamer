class MonitorTestTarg {
    static void foo3() {
        System.out.println("executing foo3");
    }
    static void foo2() {
        Object l1 = new Object();
        synchronized(l1) {
            System.out.println("executing foo2 " + l1);
            foo3();
        }
    }
    static void foo1() {
        foo2();
    }
    public static void main(String[] args){
        System.out.println("Howdy!");
        Object l1 = new Object();
        synchronized(l1) {
            System.out.println("executing main" + l1);
            foo1();
        }
    }
}
public class MonitorFrameInfo extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    List monitors;
    static int expectedCount = 2;
    static int[] expectedDepth = { 1, 3 };
    static String[] expectedNames = {"foo3", "foo2", "foo1", "main"};
    MonitorFrameInfo (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new MonitorFrameInfo(args).startTests();
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("MonitorTestTarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        int initialSize = mainThread.frames().size();
        resumeTo("MonitorTestTarg", "foo3", "()V");
        if (!mainThread.frame(0).location().method().name()
                        .equals("foo3")) {
            failure("FAILED: frame failed");
        }
        if (mainThread.frames().size() != (initialSize + 3)) {
            failure("FAILED: frames size failed");
        }
        if (mainThread.frames().size() != mainThread.frameCount()) {
            failure("FAILED: frames size not equal to frameCount");
        }
        if (vm().canGetMonitorFrameInfo()) {
            System.out.println("Get monitors");
            monitors = mainThread.ownedMonitorsAndFrames();
            if (monitors.size() != expectedCount) {
                failure("monitors count is not equal to expected count");
            }
            MonitorInfo mon = null;
            for (int j=0; j < monitors.size(); j++) {
                mon  = (MonitorInfo)monitors.get(j);
                System.out.println("Monitor obj " + mon.monitor() + "depth =" +mon.stackDepth());
                if (mon.stackDepth() != expectedDepth[j]) {
                    failure("FAILED: monitor stack depth is not equal to expected depth");
                }
            }
            stepOut(mainThread);
            boolean ok = false;
            try {
                System.out.println("*** Saved Monitor obj " + mon.monitor() + "depth =" +mon.stackDepth());
            } catch(InvalidStackFrameException ee) {
                ok = true;
                System.out.println("Got expected InvalidStackFrameException after a resume");
            }
            if (!ok) {
                failure("FAILED: MonitorInfo object was not invalidated by a resume");
            }
        } else {
            System.out.println("can not get monitors frame info");
        }
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("MonitorFrameInfo: passed");
        } else {
            throw new Exception("MonitorFrameInfo: failed");
        }
    }
}
