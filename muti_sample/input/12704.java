class HomeTarg {
    public static void main(String[] args){
        System.out.println("Howdy!");
        System.out.println("Goodbye from HomeTarg!");
    }
}
public class HomeTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    HomeTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new HomeTest(args).startTests();
    }
    private static String getValue(Map arguments, String key) {
        Connector.Argument x = (Connector.StringArgument) arguments.get(key);
        return x.value();
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("HomeTarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        EventRequestManager erm = vm().eventRequestManager();
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        Connector defaultConnector = vmm.defaultConnector();
        Map arguments = defaultConnector.defaultArguments();
        String argsHome = getValue(arguments,"home");
        String javaHome = System.getProperty("java.home");
        if (!argsHome.equals(javaHome)) {
            failure("FAILURE: Value for \"home\" does not match value for \"java.home\"");
            failure("     home is: " + argsHome);
        }
        println("java.home is: " + javaHome);
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("HomeTest: passed");
        } else {
            throw new Exception("HomeTest: failed");
        }
    }
}
