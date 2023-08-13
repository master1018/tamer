class MultiBreakpointsTarg {
    MultiBreakpointsTarg(int numThreads, int numHits) {
        for (int ii = 0; ii < numThreads; ii++) {
            console(ii, numHits);
        }
    }
    public static void main(String args[]) {
        int nthreads;
        int nhits;
        String nStr = System.getProperty("nthreads");
        if (nStr == null) {
            throw new RuntimeException("nthreads = null in debuggee");
        }
        nthreads = Integer.parseInt(nStr);
        nStr = System.getProperty("nhits");
        if (nStr == null) {
            throw new RuntimeException("nhits = null in debuggee");
        }
        nhits = Integer.parseInt(nStr);
        System.out.println("Debuggee: nthreads = " + nthreads + ", nhits = " + nhits);
        MultiBreakpointsTarg ptr = new MultiBreakpointsTarg(nthreads, nhits);
    }
    void bkpt0() {}
    void bkpt1() {}
    void bkpt2() {}
    void bkpt3() {}
    void bkpt4() {}
    void bkpt5() {}
    void bkpt6() {}
    void bkpt7() {}
    void bkpt8() {}
    void bkpt9() {}
    void bkpt10() {}
    void bkpt11() {}
    void bkpt12() {}
    void bkpt13() {}
    void bkpt14() {}
    void bkpt15() {}
    void bkpt16() {}
    void bkpt17() {}
    void bkpt18() {}
    void bkpt19() {}
    void bkpt20() {}
    void bkpt21() {}
    void bkpt22() {}
    void bkpt23() {}
    void bkpt24() {}
    void bkpt25() {}
    void bkpt26() {}
    void bkpt27() {}
    void bkpt28() {}
    void bkpt29() {}
    void console(final int num, final int nhits) {
        final InputStreamReader isr = new InputStreamReader(System.in);
        final BufferedReader    br  = new BufferedReader(isr);
        final String threadName = "" + num;
        Thread thrd = new Thread( threadName ) {
                public void run() {
                    synchronized( isr ) {
                        boolean done = false;
                        try {
                            for( int i = 0; i < nhits; i++ ) {
                                System.out.println("Thread " + threadName + " Enter a string: ");
                                String s = "test" + num;
                                switch (num) {
                                case 0: bkpt0(); break;
                                case 1: bkpt1(); break;
                                case 2: bkpt2(); break;
                                case 3: bkpt3(); break;
                                case 4: bkpt4(); break;
                                case 5: bkpt5(); break;
                                case 6: bkpt6(); break;
                                case 7: bkpt7(); break;
                                case 8: bkpt8(); break;
                                case 9: bkpt9(); break;
                                case 10: bkpt10(); break;
                                case 11: bkpt11(); break;
                                case 12: bkpt12(); break;
                                case 13: bkpt13(); break;
                                case 14: bkpt14(); break;
                                case 15: bkpt15(); break;
                                case 16: bkpt16(); break;
                                case 17: bkpt17(); break;
                                case 18: bkpt18(); break;
                                case 19: bkpt19(); break;
                                case 20: bkpt20(); break;
                                case 21: bkpt21(); break;
                                case 22: bkpt22(); break;
                                case 23: bkpt23(); break;
                                case 24: bkpt24(); break;
                                case 25: bkpt25(); break;
                                case 26: bkpt26(); break;
                                case 27: bkpt27(); break;
                                case 28: bkpt28(); break;
                                case 29: bkpt29(); break;
                                }
                                System.out.println("Thread " + threadName + " You entered : " + s);
                                if( s.compareTo( "quit" ) == 0 )
                                    done = true;
                            }
                        } catch(Exception e) {
                            System.out.println("WOOPS");
                        }
                    }
                }
            };
        thrd.setPriority(Thread.MAX_PRIORITY-1);
        thrd.start();
    }
}
public class MultiBreakpointsTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    EventRequestManager erm;
    static int nthreads;
    static int nhits;
    BreakpointRequest bkpts[];
    int hits[];
    MultiBreakpointsTest (String args[]) {
        super(args);
        bkpts = new BreakpointRequest[nthreads];
        hits = new int[nthreads];
    }
    public static void main(String[] args)      throws Exception {
        String countStr = System.getProperty("nthreads");
        if (countStr == null) {
            nthreads = 4;
        } else {
            nthreads = Integer.parseInt(countStr);
        }
        if ( nthreads > 30) {
            throw new RuntimeException("nthreads is greater than 30: " + nthreads);
        }
        countStr = System.getProperty("nhits");
        if (countStr == null) {
            nhits = 100;
        } else {
            nhits = Integer.parseInt(countStr);
        }
        args = new String[] { "-J-Dnthreads=" + nthreads, "-J-Dnhits=" + nhits} ;
        new MultiBreakpointsTest(args).startTests();
    }
    public void breakpointReached(BreakpointEvent event) {
        BreakpointRequest req = (BreakpointRequest)event.request();
        for ( int ii = 0; ii < nthreads; ii++) {
            if (req == bkpts[ii]) {
                println("Hit bkpt on thread: " +ii+ ": " +  ++hits[ii]);
                break;
            }
        }
    }
    public BreakpointRequest setBreakpoint(String clsName,
                                           String methodName,
                                           String methodSignature) {
        ReferenceType rt = findReferenceType(clsName);
        if (rt == null) {
            rt = resumeToPrepareOf(clsName).referenceType();
        }
        Method method = findMethod(rt, methodName, methodSignature);
        if (method == null) {
            throw new IllegalArgumentException("Bad method name/signature");
        }
        BreakpointRequest bpr = erm.createBreakpointRequest(method.location());
        bpr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        bpr.enable();
        return bpr;
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("MultiBreakpointsTarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        erm = vm().eventRequestManager();
        for (int ii = 0 ; ii < nthreads; ii++) {
            bkpts[ii] = setBreakpoint("MultiBreakpointsTarg",
                              "bkpt" + ii,
                              "()V");
        }
        listenUntilVMDisconnect();
        for ( int ii = 0; ii < nthreads; ii++) {
            if (hits[ii] != nhits) {
                failure("FAILED: Expected " + nhits + " breakpoints for thread " + ii + " but only got " + hits[ii]);
            }
        }
        if (!testFailed) {
            println("MultiBreakpointsTest: passed");
        } else {
            throw new Exception("MultiBreakpointsTest: failed");
        }
    }
}
