class FinalizerTarg {
    static String lockit = "lock";
    static boolean finalizerRun = false;
    static class BigObject {
        String name;
        byte[] foo = new byte[300000];
        public BigObject (String _name) {
            super();
            this.name = _name;
        }
        protected void finalize() throws Throwable {
            super.finalize();
            finalizerRun = true;
        }
    }
    static void waitForAFinalizer() {
        String s = Integer.toString(1);
        BigObject b = new BigObject (s);
        b = null; 
        System.gc();
        System.runFinalization();
        List holdAlot = new ArrayList();
        for (int chunk=10000000; chunk > 10000; chunk = chunk / 2) {
            if (finalizerRun) {
                return;
            }
            try {
                while(true) {
                    holdAlot.add(new byte[chunk]);
                    System.err.println("Allocated " + chunk);
                }
            }
            catch ( Throwable thrown ) {  
                System.gc();
            }
            System.runFinalization();
        }
        return;  
    }
    public static void main(String[] args) throws Exception {
        waitForAFinalizer();
    }
}
public class FinalizerTest extends TestScaffold {
    public static void main(String args[])
        throws Exception {
        new FinalizerTest (args).startTests();
    }
    public FinalizerTest (String args[]) {
        super(args);
    }
    protected void runTests() throws Exception {
        try {
            BreakpointEvent event0 = startToMain("FinalizerTarg");
            BreakpointEvent event1 = resumeTo("FinalizerTarg$BigObject",
                                              "finalize", "()V");
            println("Breakpoint at " +
                    event1.location().method().name() + ":" +
                    event1.location().lineNumber() + " (" +
                    event1.location().codeIndex() + ")");
            List frames = event1.thread().frames();
            List methodStack = new ArrayList(frames.size());
            Iterator iter = frames.iterator();
            while (iter.hasNext()) {
                StackFrame frame = (StackFrame) iter.next();
                methodStack.add(frame.location().declaringType().name() +
                                "." + frame.location().method().name());
            }
            println("Try a stepOverLine()...");
            StepEvent stepEvent = stepOverLine(event1.thread());
            println("Step Complete at " +
                               stepEvent.location().method().name() + ":" +
                               stepEvent.location().lineNumber() + " (" +
                               stepEvent.location().codeIndex() + ")");
            if (stepEvent.thread().frameCount() != methodStack.size()) {
                throw new Exception("Stack depths do not match: original=" +
                                    methodStack.size() +
                                    ", current=" +
                                    stepEvent.thread().frameCount());
            }
            iter = stepEvent.thread().frames().iterator();
            Iterator iter2 = methodStack.iterator();
            while (iter.hasNext()) {
                StackFrame frame = (StackFrame) iter.next();
                String name = (String) iter2.next();
                String currentName = frame.location().declaringType().name() +
                "." + frame.location().method().name();
                if (!name.equals(currentName)) {
                    throw new Exception("Stacks do not match at: original=" +
                                         name + ", current=" + currentName);
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            testFailed = true;
        } finally {
            listenUntilVMDisconnect();
        }
        if (!testFailed) {
            println("FinalizerTest: passed");
        } else {
            throw new Exception("FinalizerTest: failed");
        }
    }
}
