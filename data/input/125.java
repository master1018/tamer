class DeleteAllBkptsTarg {
    public void gus() {
    }
    public static void main(String[] args){
        System.out.println("Howdy!");
        System.out.println("Goodbye from DeleteAllBkptsTarg!");
    }
}
public class DeleteAllBkptsTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    DeleteAllBkptsTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new DeleteAllBkptsTest(args).startTests();
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("DeleteAllBkptsTarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        EventRequestManager erm = vm().eventRequestManager();
        Method method = findMethod(targetClass, "gus", "()V");
        if (method == null) {
            throw new IllegalArgumentException("Bad method name/signature");
        }
        BreakpointRequest request = erm.createBreakpointRequest(
                                                   method.location());
        try {
            erm.deleteAllBreakpoints();
        } catch (InternalException ee) {
            failure("FAIL: Unexpected Exception encountered: " + ee);
        }
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("DeleteAllBkptsTest: passed");
        } else {
            throw new Exception("DeleteAllBkptsTest: failed");
        }
    }
}
