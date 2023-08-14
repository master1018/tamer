public class StepTest extends TestScaffold {
    int maxDepth;
    String granularity;
    int expectedCount;
    int workaroundCount = 0;
    boolean lastStepNeeded = true;
    public static void main(String args[]) throws Exception {
        new StepTest(args).startTests();
    }
    StepTest(String args[]) throws Exception {
        super(args);
        maxDepth = Integer.decode(args[0]).intValue();
        granularity = args[1];
        expectedCount = Integer.decode(args[2]).intValue();
        if (args.length == 5) {
            workaroundCount = Integer.decode(args[4]).intValue();
        }
    }
    protected void runTests() throws Exception {
        String[] args2 = new String[args.length - 3];
        System.arraycopy(args, 3, args2, 0, args.length - 3);
        connect(args2);
        ThreadReference thread = waitForVMStart();
        StepEvent stepEvent = stepIntoLine(thread);
        String className = thread.frame(0).location().declaringType().name();
        System.out.println("\n\n-------Running test for class: " + className);
        BreakpointEvent bpEvent = resumeTo(className, "go", "()V");
        thread = bpEvent.thread();
        for (int i = 0; i < expectedCount; i++) {
            if (thread.frameCount() < maxDepth) {
                if (granularity.equals("line")) {
                    stepEvent = stepIntoLine(thread);
                } else {
                    stepEvent = stepIntoInstruction(thread);
                }
            } else {
                if (granularity.equals("line")) {
                    stepEvent = stepOverLine(thread);
                } else {
                    stepEvent = stepOverInstruction(thread);
                }
            }
            System.out.println("Step #" + (i+1) + "complete at " +
                               stepEvent.location().method().name() + ":" +
                               stepEvent.location().lineNumber() + " (" +
                               stepEvent.location().codeIndex() + "), frameCount = " +
                               thread.frameCount());
            if (thread.frameCount() < 2) {
                if (i == workaroundCount) {
                    lastStepNeeded = false;
                    break;
                }
                throw new Exception("Stepped too far");
            }
        }
        if (thread.frameCount() > 2) {
            throw new Exception("Didn't step far enough (" + thread.frame(0) + ")");
        }
        if (lastStepNeeded) {
            stepIntoLine(thread);
        }
        if (thread.frameCount() != 1) {
            throw new Exception("Didn't step far enough (" + thread.frame(0) + ")");
        }
        resumeToVMDisconnect();
        if (!testFailed) {
            println("StepTest: passed");
        } else {
            throw new Exception("StepTest: failed");
        }
    }
}
