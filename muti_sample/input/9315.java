class RedefineTarg {
    public static void show(String where){
        System.out.println("Returned: " + where);
    }
    public static void lastly(String where){
    }
    public static void main(String[] args){
        RedefineSubTarg sub = new RedefineSubTarg();
        String where = "";
        for (int i = 0; i < 5; ++i) {
            where = sub.foo(where);
            show(where);
        }
        lastly(where);
    }
}
public class RedefineTest extends TestScaffold {
    ReferenceType targetClass;
    static final String expected ="Boring Boring Different Boring Different ";
    int repetitionCount = 0;
    boolean beforeRedefine = true;
    RedefineTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new RedefineTest(args).startTests();
    }
    public void methodEntered(MethodEntryEvent event) {
        Method meth = event.location().method();
        ThreadReference thread = event.thread();
        if (meth.name().equals("foo")) {
            ++repetitionCount;
            beforeRedefine = true;
            try {
                expectNonObsolete(thread);
                inspectLineNumber(event, thread.frame(0));
                doRedefine(thread);
                beforeRedefine = false;
                switch (repetitionCount) {
                case 1:
                case 5:
                    expectNonObsolete(thread);
                    inspectLineNumber(event, thread.frame(0));
                    break;
                case 2:
                case 3:
                case 4:
                    expectObsolete(thread);
                    inspectLineNumber(event, thread.frame(0));
                    break;
                }
            } catch (Exception exc) {
                failure("Test Failure: unexpected exception - " + exc);
                exc.printStackTrace();
            }
        }
    }
    public void breakpointReached(BreakpointEvent event) {
        ThreadReference thread = event.thread();
        try {
            StackFrame frame = thread.frame(0);
            LocalVariable lv = frame.visibleVariableByName("where");
            Value vWhere = frame.getValue(lv);
            String remoteWhere = ((StringReference)vWhere).value();
            println("Value of where: " + remoteWhere);
            if (!remoteWhere.equals(expected)) {
                failure("FAIL: expected result string: '" + expected +
                        "' got: '" + remoteWhere + "'");
            }
        } catch (Exception thr) {
            failure("Test Failure: unexpected exception: " + thr);
        }
    }
    void expectNonObsolete(ThreadReference thread) throws Exception {
        if (isObsolete(thread)) {
            failure("FAIL: Method should NOT be obsolete");
        } else {
            println("as it should be, not obsolete");
        }
    }
    void expectObsolete(ThreadReference thread) throws Exception {
        if (isObsolete(thread)) {
            println("obsolete like it should be");
        } else {
            failure("FAIL: Method should be obsolete");
        }
    }
    void inspectLineNumber(LocatableEvent event, StackFrame frame) throws Exception {
        int n = -1;
        int expectedLine = -1;
        switch (repetitionCount) {
        case 1:
            expectedLine = 4;
            break;
        case 2:
            expectedLine = beforeRedefine ? 4:21;
            break;
        case 3:
            expectedLine = beforeRedefine ? 21:4;
            break;
        case 4:
            expectedLine = beforeRedefine ? 4:21;
            break;
        case 5:
            expectedLine = 21;
            break;
        }
        Method method = event.location().method();
        if (frame.location().method().isObsolete()) {
            println("inspectLineNumber skipping obsolete method " + method.name());
        } else {
            n = method.location().lineNumber();
            int m = frame.location().lineNumber();
            if ((n != expectedLine) || (n != m)) {
                failure("Test Failure: line number disagreement: " +
                        n + " (event) versus " + m + " (frame) versus " + expectedLine +
                        " (expected)");
            } else {
                println("inspectLineNumber in method " + method.name() + " at line " + n);
            }
        }
    }
    boolean isObsolete(ThreadReference thread) throws Exception {
        StackFrame frame = thread.frame(0);
        Method meth = frame.location().method();
        return meth.isObsolete();
    }
    void doRedefine(ThreadReference thread) throws Exception {
        Exception receivedException = null;
        String fileName = "notThis";
        switch (repetitionCount) {
        case 1:
            fileName = "RedefineSubTarg.class";
            break;
        case 2:
            fileName = "Different_RedefineSubTarg.class";
            break;
        case 3:
            fileName = "RedefineSubTarg.class";
            break;
        case 4:
            fileName = "Different_RedefineSubTarg.class";
            break;
        case 5:
            fileName = "SchemaChange_RedefineSubTarg.class";
            break;
        }
        File phyl = new File(fileName);
        byte[] bytes = new byte[(int)phyl.length()];
        InputStream in = new FileInputStream(phyl);
        in.read(bytes);
        in.close();
        Map map = new HashMap();
        map.put(findReferenceType("RedefineSubTarg"), bytes);
        println(System.getProperty("line.separator") + "Iteration # " + repetitionCount +
                " ------ Redefine as: " + fileName);
        try {
            vm().redefineClasses(map);
        } catch (Exception thr) {
            receivedException = thr;
        }
        switch (repetitionCount) {
        case 5:
            if (receivedException == null) {
                failure("FAIL: no exception; expected: UnsupportedOperationException");
            } else if (receivedException instanceof UnsupportedOperationException) {
                println("Received expected exception: " + receivedException);
            } else {
                failure("FAIL: got exception: " + receivedException +
                        ", expected: UnsupportedOperationException");
            }
            break;
        default:
            if (receivedException != null) {
                failure("FAIL: unexpected exception: " +
                        receivedException);
            }
            break;
        }
        return;
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("RedefineTarg");
        targetClass = bpe.location().declaringType();
        EventRequestManager erm = vm().eventRequestManager();
        MethodEntryRequest mee = erm.createMethodEntryRequest();
        mee.addClassFilter("RedefineSubTarg");
        mee.enable();
        List lastlys = targetClass.methodsByName("lastly");
        if (lastlys.size() != 1) {
            throw new Exception ("TestFailure: Expected one 'lastly' method, found: " +
                                 lastlys);
        }
        Location loc = ((Method)(lastlys.get(0))).location();
        EventRequest req = erm.createBreakpointRequest(loc);
        req.enable();
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("RedefineTest: passed");
        } else {
            throw new Exception("RedefineTest: failed");
        }
    }
}
