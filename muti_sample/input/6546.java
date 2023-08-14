public class LaunchCommandLine extends JDIScaffold {
    public static void main(String args[]) throws Exception {
        new LaunchCommandLine(args).startTests();
    }
    LaunchCommandLine(String args[]) {
        super();
    }
    protected void runTests() throws Exception {
        String[] args = new String[2];
        args[0] = "-connect";
        args[1] = "com.sun.jdi.CommandLineLaunch:main=HelloWorld a b c \"a b c\"";
        testArgs(args);
        System.out.println("com.sun.jdi.CommandLineLaunch: passed");
    }
    void testArgs(String[] args) throws Exception {
        connect(args);
        waitForVMStart();
        BreakpointEvent bp = resumeTo("HelloWorld", "main", "([Ljava/lang/String;)V");
        StackFrame frame = bp.thread().frame(0);
        LocalVariable argsVariable = frame.visibleVariableByName("args");
        ArrayReference argsArray = (ArrayReference)frame.getValue(argsVariable);
        List argValues = argsArray.getValues();
        if (argValues.size() != 4) {
            throw new Exception("Wrong number of command line arguments: " + argValues.size());
        }
        String string = ((StringReference)argValues.get(0)).value();
        if (!string.equals("a")) {
            throw new Exception("Bad command line argument value: " + string);
        }
        string = ((StringReference)argValues.get(1)).value();
        if (!string.equals("b")) {
            throw new Exception("Bad command line argument value: " + string);
        }
        string = ((StringReference)argValues.get(2)).value();
        if (!string.equals("c")) {
            throw new Exception("Bad command line argument value: " + string);
        }
        string = ((StringReference)argValues.get(3)).value();
        if (!string.equals("a b c")) {
            throw new Exception("Bad command line argument value: " + string);
        }
        resumeToVMDeath();
    }
}
