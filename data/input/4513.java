class SourceNameFilterTarg {
    static  void bkpt() {
    }
    public static void main(String[] args){
        System.out.println("Howdy!");
        LoadedLater1.doit();
        bkpt();
        LoadedLater2.doit();
        bkpt();
        LoadedLater3.doit();
        bkpt();
        System.out.println("Goodbye from SourceNameFilterTarg!");
    }
}
class LoadedLater1 {
    public static void doit() {
        System.out.println("didit1");
    }
}
class LoadedLater2 {
    public static void doit() {
        System.out.println("didit2");
    }
}
class LoadedLater3 {
    public static void doit() {
        System.out.println("didit3");
    }
}
public class SourceNameFilterTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    boolean gotEvent1 = false;
    boolean gotEvent2 = false;
    boolean gotEvent3 = false;
    ClassPrepareRequest cpReq;
    boolean shouldResume = false;
    SourceNameFilterTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new SourceNameFilterTest(args).startTests();
    }
    public void eventSetComplete(EventSet set) {
        if (shouldResume) {
            set.resume();
            shouldResume = false;
        }
    }
    public void classPrepared(ClassPrepareEvent event) {
        shouldResume = true;
        ReferenceType rt = event.referenceType();
        String rtname = rt.name();
        if (rtname.equals("LoadedLater1")) {
            gotEvent1 = true;
        }
        if (rtname.equals("LoadedLater2")) {
            gotEvent2 = true;
        }
        if (rtname.equals("LoadedLater3")) {
            gotEvent3 = true;
        }
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
            println("\nAvailable strata:  " + rt.availableStrata());
        }
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("SourceNameFilterTarg");
        targetClass = bpe.location().declaringType();
        boolean noSourceName = false;
        try {
            targetClass.sourceName();
        } catch (AbsentInformationException ee) {
            noSourceName = true;
        }
        if (noSourceName) {
            println("-- Running with no source names");
        } else {
            println("-- Running with source names");
        }
        mainThread = bpe.thread();
        EventRequestManager erm = vm().eventRequestManager();
        addListener(this);
        cpReq = erm.createClassPrepareRequest();
        cpReq.enable();
        resumeTo("SourceNameFilterTarg", "bkpt", "()V");
        cpReq.disable();
        cpReq.addSourceNameFilter("jj");
        cpReq.enable();
        resumeTo("SourceNameFilterTarg", "bkpt", "()V");
        cpReq.disable();
        cpReq = erm.createClassPrepareRequest();
        cpReq.addSourceNameFilter("SourceNameFilterTest.java");
        cpReq.enable();
        resumeTo("SourceNameFilterTarg", "bkpt", "()V");
        listenUntilVMDisconnect();
        if (!gotEvent1) {
            failure("failure: Did not get a class prepare request " +
                    "for LoadedLater1");
        }
        if (gotEvent2) {
            failure("failure: Did get a class prepare request " +
                    "for LoadedLater2");
        }
        if (gotEvent3 && noSourceName) {
            failure("failure: Did get a class prepare request " +
                    "for LoadedLater3");
        }
        else if (!gotEvent3 && !noSourceName) {
            failure("failure: Did not get a class prepare request " +
                    "for LoadedLater3");
        }
        if (!testFailed) {
            println("SourceNameFilterTest: passed");
        } else {
            throw new Exception("SourceNameFilterTest: failed");
        }
    }
}
