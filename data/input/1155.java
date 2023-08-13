class GetLocalVariables2Targ {
    static boolean bar(int i) {
        if (i < 2) {
            return true;
        } else {
            return false;
        }
    }
    public static void main(String[] args) {
        int i = 1;
        String command;
        if (i == 0) {
            command = "0";
        } else if (bar(i)) {
            command = "1";
        } else {
            command = "2";
        }
    }
}
public class GetLocalVariables2Test extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    GetLocalVariables2Test (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new GetLocalVariables2Test(args).startTests();
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("GetLocalVariables2Targ");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        EventRequestManager erm = vm().eventRequestManager();
        bpe = resumeTo("GetLocalVariables2Targ", "bar", "(I)Z");
        StackFrame frame = bpe.thread().frame(1);
        List localVars = frame.visibleVariables();
        System.out.println("    Visible variables at this point are: ");
        for (Iterator it = localVars.iterator(); it.hasNext();) {
            LocalVariable lv = (LocalVariable) it.next();
            System.out.print(lv.name());
            System.out.print(" typeName: ");
            System.out.print(lv.typeName());
            System.out.print(" signature: ");
            System.out.print(lv.type().signature());
            System.out.print(" primitive type: ");
            System.out.println(lv.type().name());
            if("command".equals(lv.name())) {
                failure("Failure: LocalVariable \"command\" should not be visible at this point.");
                if (lv.isVisible(frame)) {
                    System.out.println("Failure: \"command.isvisible(frame)\" returned true.");
                }
            }
        }
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("GetLocalVariables2Test: passed");
        } else {
            throw new Exception("GetLocalVariables2Test: failed");
        }
    }
}
