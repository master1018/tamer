public class sagtest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    sagtest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new sagtest(args).startTests();
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("sagtarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        EventRequestManager erm = vm().eventRequestManager();
        stepOverLine(mainThread);  
        stepOverLine(mainThread);  
        stepOverLine(mainThread);  
        stepOverLine(mainThread);  
        stepOverLine(mainThread);  
        sagdoit mine = new sagdoit(vm());
        mine.doAll();
        if (!testFailed) {
            println("sagtest: passed");
        } else {
            throw new Exception("sagtest: failed");
        }
    }
}
