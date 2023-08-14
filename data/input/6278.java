class CountEventTarg {
    static CountEventTarg first = new CountEventTarg();
    static CountEventTarg second = new CountEventTarg();
    static CountEventTarg third = new CountEventTarg();
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
public class CountEvent extends TestScaffold {
    public static void main(String args[]) throws Exception {
        new CountEvent(args).startTests();
    }
    CountEvent(String args[]) throws Exception {
        super(args);
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("CountEventTarg");
        ThreadReference thread = bpe.thread();
        StepEvent stepEvent = stepIntoLine(thread);
        ReferenceType clazz = thread.frame(0).location().declaringType();
        String className = clazz.name();
        BreakpointEvent bpEvent = resumeTo(className, "go", "()V");
        bpEvent.request().disable();
        System.out.println("About to resume");
        resumeToVMDisconnect();
        if (!testFailed) {
            println("CountEvent: passed");
        } else {
            throw new Exception("CountEvent: failed");
        }
    }
}
