class t2 {
    public static void sayHello1(int i, int j) {
        sayHello2(i, j);
    }
    public static void sayHello2(int i, int j) {
        sayHello3(i, j);
    }
    public static void sayHello3(int i, int j) {
        sayHello4(i, j);
    }
    public static void sayHello4(int i, int j) {
        sayHello5(i, j);
    }
    public static void sayHello5(int i, int j) {
        if (i < 2) {
            sayHello1(++i, j);
        } else {
            System.out.print  ("MethodEntryExitEventsDebugee: ");
            System.out.print  ("    -->> Hello.  j is: ");
            System.out.print  (j);
            System.out.println(" <<--");
        }
    }
}
class MethodEntryExitEventsDebugee {
    public static void loopComplete () {
        StringBuffer sb = new StringBuffer();
        sb.append ("MethodEntryExitEventsDebugee: ");
        sb.append ("Executing loopComplete method for a graceful shutdown...");
        String s = sb.toString();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            System.out.print(c);
        }
        System.out.println();
    }
    public static void main(String[] args) {
        t2 test = new t2();
        for (int j = 0; j < 3; j++) {
            test.sayHello1(0, j);
        }
        loopComplete();
    }
}
public class MethodEntryExitEvents extends TestScaffold {
    int sessionSuspendPolicy = EventRequest.SUSPEND_ALL;
    StepRequest stepReq = null; 
    boolean finishedCounting = false;
    final int expectedEntryCount = 1 + 1 + (15 * 3) + 1;
    int methodEntryCount = 0;
    final int expectedExitCount = 1 + (15 * 3);
    int methodExitCount = 0;
    private String[] excludes = {"java.*", "javax.*", "sun.*",
                                 "com.sun.*"};
    MethodEntryExitEvents (String args[]) {
        super(args);
    }
    private void usage(String[] args) throws Exception {
        StringBuffer sb = new StringBuffer("Usage: ");
        sb.append(System.getProperty("line.separator"));
        sb.append("  java ");
        sb.append(getClass().getName());
        sb.append(" [SUSPEND_NONE | SUSPEND_EVENT_THREAD | SUSPEND_ALL]");
        sb.append(" [MethodEntryExitEventsDebugee | -connect <connector options...>] ");
        throw new Exception (sb.toString());
    }
    public static void main(String[] args)      throws Exception {
        MethodEntryExitEvents meee = new MethodEntryExitEvents (args);
        meee.startTests();
    }
    public void exceptionThrown(ExceptionEvent event) {
        System.out.println("Exception: " + event.exception());
        System.out.println(" at catch location: " + event.catchLocation());
        if (stepReq == null) {
            stepReq =
                eventRequestManager().createStepRequest(event.thread(),
                                                        StepRequest.STEP_MIN,
                                                        StepRequest.STEP_INTO);
            stepReq.addCountFilter(1);  
            stepReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        }
        stepReq.enable();
    }
    public void stepCompleted(StepEvent event) {
        System.out.println("stepCompleted: line#=" +
                           event.location().lineNumber() +
                           " event=" + event);
        StepRequest str= (StepRequest)event.request();
        str.disable();
    }
    public void methodEntered(MethodEntryEvent event) {
        if (! finishedCounting) {
            methodEntryCount++;
            System.out.print  (" Method entry number: ");
            System.out.print  (methodEntryCount);
            System.out.print  ("  :  ");
            System.out.println(event);
            if ("loopComplete".equals(event.method().name())) {
                finishedCounting = true;
            }
        }
    }
    public void methodExited(MethodExitEvent event) {
        if (! finishedCounting){
            methodExitCount++;
            System.out.print  (" Method exit  number: ");
            System.out.print  (methodExitCount);
            System.out.print  ("  :  ");
            System.out.println(event);
        }
    }
    protected void runTests() throws Exception {
        if (args.length < 1) {
            usage(args);
        }
        if ("SUSPEND_NONE".equals(args[0])) {
            sessionSuspendPolicy = EventRequest.SUSPEND_NONE;
        } else if ("SUSPEND_EVENT_THREAD".equals(args[0])) {
            sessionSuspendPolicy = EventRequest.SUSPEND_EVENT_THREAD;
        } else if ("SUSPEND_ALL".equals(args[0])) {
            sessionSuspendPolicy = EventRequest.SUSPEND_ALL;
        } else {
            usage(args);
        }
        System.out.print("Suspend policy is: ");
        System.out.println(args[0]);
        String[] args2 = new String[args.length - 1];
        System.arraycopy(args, 1, args2, 0, args.length - 1);
        if (args2.length < 1) {
            usage(args2);
        }
        List argList = new ArrayList(Arrays.asList(args2));
        System.out.println("run args: " + argList);
        connect((String[]) argList.toArray(args2));
        waitForVMStart();
        try {
            ExceptionRequest exceptionRequest =
                eventRequestManager().createExceptionRequest(null, 
                                                             true, 
                                                             true);
            exceptionRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
            exceptionRequest.enable();
            MethodEntryRequest entryRequest =
               eventRequestManager().createMethodEntryRequest();
            for (int i=0; i<excludes.length; ++i) {
                entryRequest.addClassExclusionFilter(excludes[i]);
            }
            entryRequest.setSuspendPolicy(sessionSuspendPolicy);
            entryRequest.enable();
            MethodExitRequest exitRequest =
                eventRequestManager().createMethodExitRequest();
            for (int i=0; i<excludes.length; ++i) {
                exitRequest.addClassExclusionFilter(excludes[i]);
            }
            exitRequest.setSuspendPolicy(sessionSuspendPolicy);
            exitRequest.enable();
            listenUntilVMDisconnect();
            System.out.println("All done...");
        } catch (Exception ex){
            ex.printStackTrace();
            testFailed = true;
        }
        if ((methodEntryCount != expectedEntryCount) ||
            (methodExitCount != expectedExitCount)) {
            testFailed = true;
        }
        if (!testFailed) {
            System.out.println();
            System.out.println("MethodEntryExitEvents: passed");
            System.out.print  ("    Method entry count: ");
            System.out.println(methodEntryCount);
            System.out.print  ("    Method exit  count: ");
            System.out.println(methodExitCount);
        } else {
            System.out.println();
            System.out.println("MethodEntryExitEvents: failed");
            System.out.print  ("    expected method entry count: ");
            System.out.println(expectedEntryCount);
            System.out.print  ("    observed method entry count: ");
            System.out.println(methodEntryCount);
            System.out.print  ("    expected method exit  count: ");
            System.out.println(expectedExitCount);
            System.out.print  ("    observed method exit  count: ");
            System.out.println(methodExitCount);
            throw new Exception("MethodEntryExitEvents: failed");
        }
    }
}
