class InstanceFilterTarg {
    static InstanceFilterTarg first = new InstanceFilterTarg();
    static InstanceFilterTarg second = new InstanceFilterTarg();
    static InstanceFilterTarg third = new InstanceFilterTarg();
    public static void main(String args[]) {
        start();
    }
    static void start() {
        first.go();
        second.go();
        third.go();
    }
    void go() {
        one();
        two();
        three();
    }
    void one() {
    }
    void two() {
    }
    void three() {
    }
}
public class InstanceFilter extends TestScaffold {
    ReferenceType targetClass;
    ObjectReference theInstance;
    MethodEntryRequest methodEntryRequest;
    int methodCount = 0;
    String[] expectedMethods = new String[] { "go", "one", "two", "three"};
    public static void main(String args[]) throws Exception {
        new InstanceFilter(args).startTests();
    }
    InstanceFilter(String args[]) throws Exception {
        super(args);
    }
    public void methodEntered(MethodEntryEvent event) {
        if (testFailed) {
            return;
        }
        ObjectReference theThis;
        try {
            theThis = event.thread().frame(0).thisObject();
        } catch (IncompatibleThreadStateException ee) {
            failure("FAILED: Exception occured in methodEntered: " + ee);
            return;
        }
        if (theThis == null) {
            methodEntryRequest.disable();
            return;
        }
        if (!theThis.equals(theInstance)) {
            failure("FAILED: Got a hit on a non-selected instance");
        }
        {
            String methodStr = event.location().method().name();
            if (methodCount >= expectedMethods.length) {
                failure("FAILED: Got too many methodEntryEvents");
            } else if (methodStr.indexOf(expectedMethods[methodCount]) == -1) {
                failure("FAILED: Expected method: " + expectedMethods[methodCount]);
            }
            methodCount++;
            println("Method: " + methodStr);
        }
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startTo("InstanceFilterTarg", "go", "()V");
        targetClass = bpe.location().declaringType();
        Field field = targetClass.fieldByName("second");
        theInstance = (ObjectReference)(targetClass.getValue(field));
        EventRequestManager mgr = vm().eventRequestManager();
        methodEntryRequest = mgr.createMethodEntryRequest();
        methodEntryRequest.addInstanceFilter(theInstance);
        methodEntryRequest.enable();
        listenUntilVMDisconnect();
        if (!testFailed && methodCount < expectedMethods.length) {
            failure("FAILED: Expected " + expectedMethods.length + " events, only got "
                    + methodCount);
        }
        if (!testFailed) {
            println("InstanceFilter: passed");
        } else {
            throw new Exception("InstanceFilter: failed");
        }
    }
}
