class LineNumberOnBraceTarg {
    public final static int stopLine = 28;   
    public final static int stopLine2 = 34;  
    public static void main(String[] args){
        System.out.println("Howdy!");
        if (args.length == 0) {
            System.out.println("No args to debuggee");             
        } else {
            System.out.println("Some args to debuggee");
        }
        if (args.length == 0) {
            boolean b1 = false;
            if (b1) {                                              
                System.out.println("In 2nd else");                 
            }
        } else {
            System.out.println("In 2nd else");
        }
        System.out.println("Goodbye from LineNumberOnBraceTarg!");  
    }
    public void exampleOfThrow() {
        try {
            throw new Exception();
        } catch (Exception e) {
            System.out.println("caught exception");
        } finally {
            System.out.println("finally");
        }
    }
}
public class LineNumberOnBraceTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    LineNumberOnBraceTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new LineNumberOnBraceTest(args).startTests();
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("LineNumberOnBraceTarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        resumeTo("LineNumberOnBraceTarg", LineNumberOnBraceTarg.stopLine);
        StepEvent stepev = stepOverLine(mainThread);       
        int ln = stepev.location().lineNumber();
        System.out.println("Debuggee is stopped at line " + ln);
        if (ln != LineNumberOnBraceTarg.stopLine + 4) {
            failure("FAIL: Bug 4952629: Should be at line " +
                    (LineNumberOnBraceTarg.stopLine + 4) +
                    ", am at " + ln);
        } else {
            System.out.println("Passed test for 4952629");
        }
        System.out.println("Resuming to " + LineNumberOnBraceTarg.stopLine2);
        resumeTo("LineNumberOnBraceTarg", LineNumberOnBraceTarg.stopLine2);
        System.out.println("Stopped at " + LineNumberOnBraceTarg.stopLine2);
        stepev = stepOverLine(mainThread);
        ln = stepev.location().lineNumber();
        System.out.println("Debuggee is stopped at line " + ln);
        if (ln == LineNumberOnBraceTarg.stopLine2 + 1) {
            failure("FAIL: bug 4870514: Incorrectly stopped at " +
                    (LineNumberOnBraceTarg.stopLine2 + 1));
        } else {
            System.out.println("Passed test for 4870514");
        }
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("LineNumberOnBraceTest: passed");
        } else {
            throw new Exception("LineNumberOnBraceTest: failed");
        }
    }
}
