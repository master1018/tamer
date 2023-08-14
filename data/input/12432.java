class SDENullTarg {
    public static void main(String[] args){
        System.out.println("Howdy!");
        System.out.println("Goodbye from SDENullTarg!");
    }
}
public class SDENullTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    SDENullTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new SDENullTest(args).startTests();
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("SDENullTarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        EventRequestManager erm = vm().eventRequestManager();
        vm().setDefaultStratum(null);
        if (!testFailed) {
            println("SDENullTest: passed");
        } else {
            throw new Exception("SDENullTest: failed");
        }
    }
}
