class FilterMangleTarg {
    public static void bkpt() {
    }
    public static void main(String[] args) {
        System.out.println("calling mangle");
        onion.pickle.Mangle.main(args);
        System.out.println("calling mangle");
        bkpt();
        System.out.println("bkpt done");
    }
}
public class FilterMangleTest extends TestScaffold {
    ClassPrepareRequest cpReq;
    boolean shouldResume = false;
    boolean gotIt = false;
    static boolean shouldPass = true;
    static String pattern;
    static final String op = "onion" + File.separator + "pickle" + File.separator;
    ReferenceType targetClass;
    FilterMangleTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        testSetUp();
        if (args.length != 0) {
            if (args[0].startsWith("SDE-")) {
                if (args[0].charAt(4) == 'f') {
                    shouldPass = false;
                }
                pattern = args[0].substring(5);
                String[] args2 = new String[args.length - 1];
                System.arraycopy(args, 1, args2, 0, args.length - 1);
                new FilterMangleTest(args2).startTests();
                return;
            }
            pattern = "Mangle.java";
        } else {
            pattern = "Mangle.java";
        }
        new FilterMangleTest(args).startTests();
    }
    static void testSetUp() throws Exception {
        InstallSDE.install(new File(System.getProperty("test.classes", "."),
                                    op + "Mangle.class"),
                           new File(System.getProperty("test.src", "."),
                                    "Mangle.sde"));
    }
    public void eventSetComplete(EventSet set) {
        if (shouldResume) {
            set.resume();
            shouldResume = false;
        }
    }
    public void classPrepared(ClassPrepareEvent event) {
        if (event.request() == cpReq) {
            ReferenceType rt = event.referenceType();
            String rtname = rt.name();
            if (rtname.equals("onion.pickle.Mangle")) {
                gotIt = true;
            }
            shouldResume = true;
            if (false) {
                println("Got ClassPrepareEvent for : " + rtname);
                try {
                    println("    sourceName = " + rt.sourceName());
                } catch (AbsentInformationException ee) {
                    failure("failure: absent info on sourceName(): " + ee);
                }
                String stratum = rt.defaultStratum();
                println("    defaultStratum = " + stratum);
                try {
                    println("    sourceNames = " + rt.sourceNames(stratum));
                } catch (AbsentInformationException ee) {
                    failure("failure: absent info on sourceNames(): " + ee);
                }
                println("Available strata:  " + rt.availableStrata() + "\n");
            }
        }
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("FilterMangleTarg");
        targetClass = bpe.location().declaringType();
        if (!vm().canGetSourceDebugExtension()) {
            failure("FAIL: canGetSourceDebugExtension() is false");
        } else {
            println("canGetSourceDebugExtension() is true");
        }
        EventRequestManager erm = vm().eventRequestManager();
        cpReq = erm.createClassPrepareRequest();
        if (true)  {
            cpReq.addSourceNameFilter(pattern);
        } else {
            cpReq.addSourceNameFilter("Mangle.j*");
            cpReq.addSourceNameFilter("Mangle.jav*");
        }
        cpReq.enable();
        addListener(this);
        resumeTo("FilterMangleTarg", "bkpt", "()V");
        listenUntilVMDisconnect();
        if (!gotIt) {
            if (shouldPass) {
                failure("FAIL: Did not get class prepare event for " +
                    "onion.pickle.Mangle, pattern = " + pattern);
            }
        } else {
            if (!shouldPass) {
                failure("FAIL: Got unexpected class prepare event for " +
                    "onion.pickle.Mangle, pattern = " + pattern);
            }
        }
        if (!testFailed) {
            println("FilterMangleTest: passed: pattern = " + pattern);
        } else {
            throw new Exception("FilterMangleTest: failed");
        }
    }
}
