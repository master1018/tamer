public class MangleStepTest extends TestScaffold {
    static final String op = "onion" + File.separator + "pickle" + File.separator;
    ReferenceType targetClass;
    final String stratum;
    static boolean aTestFailed = false;
    MangleStepTest (String stratum) {
        super(new String[0]);
        this.stratum = stratum;
    }
    public static void main(String[] args)      throws Exception {
        testSetUp();
        new MangleStepTest(args[0]).startTests();
        if (aTestFailed) {
            throw new Exception("MangleStepTest: failed");
        }
    }
    static void testSetUp() throws Exception {
        InstallSDE.install(new File(System.getProperty("test.classes", "."),
                                    op + "Mangle.class"),
                           new File(System.getProperty("test.src", "."),
                                    "Mangle.sde"));
    }
    void lineMatch(Location loc, int javaLine, int defaultLine) {
        if (loc.lineNumber() != defaultLine) {
            failure("FAIL: at " + loc.lineNumber() +
                    ", expected " + defaultLine);
        } else {
            println("at: " + loc.lineNumber());
        }
        if (loc.lineNumber("Java") != javaLine) {
            failure("FAIL: at Java line " + loc.lineNumber("Java") +
                    ", expected " + javaLine);
        }
    }
    protected void runTests() throws Exception {
        int[] lines;
        int[] jLines;
        String targetName = "onion.pickle.Mangle";
        startUp(targetName);
        if (!stratum.equals("unset")) {
            vm().setDefaultStratum(stratum);
        }
        BreakpointEvent bpe = resumeTo(targetName, "main",
                                       "([Ljava/lang/String;)V");
        waitForInput();
        ThreadReference thread = bpe.thread();
        if (stratum.equals("Java")) {
            lines = new int[] {4, 5, 6, 7, 8, 9};
            jLines = new int[] {4, 5, 6, 7, 8, 9};
        } else if (stratum.equals("Rats")) {
            lines = new int[] {1000, 1111, 1112};
            jLines = new int[] {4, 5, 7};
        } else  {  
            lines = new int[] {200, 210, 217, 218};
            jLines = new int[] {4, 7, 8, 9};
        }
        println("Testing stratum: " + stratum);
        lineMatch(bpe.location(), jLines[0], lines[0]);
        for (int i = 1; i < lines.length; ++i) {
            StepEvent se = stepOverLine(thread);
            lineMatch(se.location(), jLines[i], lines[i]);
        }
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("MangleStepTest (" + stratum + "): passed");
        } else {
            println("MangleStepTest (" + stratum + "): failed");
            aTestFailed = true;
        }
    }
}
