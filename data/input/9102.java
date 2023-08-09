class PopAsynchronousTarg {
    static final int N = 30;
    int fibonacci(int n) {
        if (n <= 2) {
            return 1;
        } else {
            return fibonacci(n-1) + fibonacci(n-2);
        }
    }
    void report(int n, int result) {
        System.out.println("fibonacci(" + n + ") = " + result);
    }
    public static void main(String[] args){
        int n = N;
        System.out.println("Howdy!");
        PopAsynchronousTarg pat = new PopAsynchronousTarg();
        pat.report(n, pat.fibonacci(n));
        System.out.println("Goodbye from PopAsynchronousTarg!");
    }
}
public class PopAsynchronousTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    int result = -1;
    boolean harassTarget = true;
    Object harassLock = new Object();
    PopAsynchronousTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new PopAsynchronousTest(args).startTests();
    }
    public void breakpointReached(BreakpointEvent event) {
        harassTarget = false;
        synchronized(harassLock) {
            try {
                StackFrame frame = event.thread().frame(0);
                LocalVariable lv = frame.visibleVariableByName("result");
                IntegerValue resultV = (IntegerValue)frame.getValue(lv);
                result = resultV.value();
            } catch (Exception exc) {
                exc.printStackTrace(System.err);
                failure("TEST FAILURE: exception " + exc);
            }
        }
    }
    class HarassThread extends Thread {
        public void run() {
            int harassCount = 0;
            try {
                int prev = 0;
                int delayTime = 1;
                synchronized(harassLock) {
                    while (harassTarget && (harassCount < 10)) {
                        boolean backoff = true;
                        mainThread.suspend();
                        StackFrame top = mainThread.frame(0);
                        Method meth = top.location().method();
                        String methName = meth.name();
                        if (methName.equals("fibonacci")) {
                            LocalVariable lv = top.visibleVariableByName("n");
                            IntegerValue nV = (IntegerValue)top.getValue(lv);
                            int n = nV.value();
                            if (n != prev) {
                                backoff = false;
                                StackFrame popThis = top;
                                Iterator it = mainThread.frames().iterator();
                                while (it.hasNext()) {
                                    StackFrame frame = (StackFrame)it.next();
                                    if (!frame.location().method().name().equals("fibonacci")) {
                                        break;
                                    }
                                    popThis = frame;
                                }
                                println("popping fibonacci(" + n + ")");
                                mainThread.popFrames(popThis);
                                ++harassCount;
                                prev = n;
                            } else {
                                println("ignoring another fibonacci(" + n + ")");
                            }
                        } else {
                            println("ignoring " + methName);
                        }
                        if (backoff) {
                            delayTime *= 2;
                        } else {
                            delayTime /= 2;
                            if (delayTime < harassCount) {
                                delayTime = harassCount;
                            }
                        }
                        mainThread.resume();
                        println("Delaying for " + delayTime + "ms");
                        Thread.sleep(delayTime);
                    }
                }
            } catch (Exception exc) {
                exc.printStackTrace(System.err);
                failure("TEST FAILURE: exception " + exc);
            }
            println("Harassment complete, count = " + harassCount);
        }
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("PopAsynchronousTarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        EventRequestManager erm = vm().eventRequestManager();
        List meths = targetClass.methodsByName("report");
        Location loc = ((Method)(meths.get(0))).location();
        BreakpointRequest request = erm.createBreakpointRequest(loc);
        request.enable();
        (new HarassThread()).start();
        listenUntilVMDisconnect();
        int correct = (new PopAsynchronousTarg()).
            fibonacci(PopAsynchronousTarg.N);
        if (result == correct) {
            println("Got expected result: " + result);
        } else {
            failure("FAIL: expected result: " + correct +
                    ", got: " + result);
        }
        if (!testFailed) {
            println("PopAsynchronousTest: passed");
        } else {
            throw new Exception("PopAsynchronousTest: failed");
        }
    }
}
