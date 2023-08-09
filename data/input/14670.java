public class CheckAnnotations
    extends Activatable implements MyRMI, Runnable
{
    private static Object dummy = new Object();
    private static MyRMI myRMI = null;
    private static ByteArrayOutputStream rmidOut = new ByteArrayOutputStream();
    private static ByteArrayOutputStream rmidErr = new ByteArrayOutputStream();
    public static void main(String args[]) {
        Object dummy1 = new Object();
        RMID rmid = null;
        System.err.println("\nRegression test for bug/rfe 4109103\n");
        try {
            TestLibrary.suggestSecurityManager(TestParams.defaultSecurityManager);
            RMID.removeLog();
            rmid = RMID.createRMID(rmidOut, rmidErr, false);
            rmid.start();
            Properties p = new Properties();
            p.put("java.security.policy",
                  TestParams.defaultGroupPolicy);
            p.put("java.security.manager",
                  TestParams.defaultSecurityManager);
            System.err.println("Create activation group in this VM");
            ActivationGroupDesc groupDesc =
                new ActivationGroupDesc(p, null);
            ActivationSystem system = ActivationGroup.getSystem();
            ActivationGroupID groupID = system.registerGroup(groupDesc);
            ActivationGroup.createGroup(groupID, groupDesc, 0);
            ActivationDesc desc = new ActivationDesc
                ("CheckAnnotations", null, null);
            myRMI = (MyRMI) Activatable.register(desc);
            for (int i = 0; i < 3; i++) {
                if(!checkAnnotations(i-1)) {
                    TestLibrary.bomb("Test failed: output improperly annotated.");
                }
                System.err.println
                    ("Deactivate object via method call");
                myRMI.shutdown();
            }
            System.err.println
                ("\nsuccess: CheckAnnotations test passed ");
        } catch (Exception e) {
            TestLibrary.bomb("\nfailure: unexpected exception ", e);
        } finally {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
            }
            myRMI = null;
            System.err.println("rmid shut down");
            ActivationLibrary.rmidCleanup(rmid);
        }
    }
    public static boolean checkAnnotations(int iteration)
        throws IOException
    {
        try {
            Thread.sleep(5000);
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
        myRMI.printOut("out" + iteration);
        myRMI.printErr("err" + iteration);
        myRMI.printOut("out" + iteration);
        myRMI.printErr("err" + iteration);
        String outString = null;
        String errString = null;
        for (int i = 0 ; i < 5 ; i ++ ) {
            try {
                Thread.sleep(4000);
            } catch(InterruptedException e) {
            }
            outString = rmidOut.toString();
            errString = rmidErr.toString();
            if ((!outString.equals("")) &&
                (!errString.equals("")))
            {
                System.err.println("obtained annotations");
                break;
            }
            System.err.println("rmid output not yet received, retrying...");
        }
        rmidOut.reset();
        rmidErr.reset();
        if (iteration >= 0) {
            System.err.println("Checking annotations...");
            System.err.println(outString);
            System.err.println(errString);
            StringTokenizer stOut = new StringTokenizer(outString, ":");
            StringTokenizer stErr = new StringTokenizer(errString, ":");
            String execErr = null;
            String execOut = null;
            String destOut = null;
            String destErr = null;
            String outTmp  = null;
            String errTmp  = null;
            while (stOut.hasMoreTokens()) {
                execOut = outTmp;
                outTmp  = destOut;
                destOut = stOut.nextToken();
            }
            while (stErr.hasMoreTokens()) {
                execErr = errTmp;
                errTmp  = destErr;
                destErr = stErr.nextToken();
            }
            if ((execErr == null)||(errTmp == null)||
                (destErr == null)) {
                return false;
            }
            if ((execOut == null)||(outTmp == null)||
                (destOut == null)) {
                return false;
            }
            if (execOut.equals("ExecGroup-" + iteration)
                && (new String(destOut.substring(0,4)).equals("out" +
                                                              iteration))
                && (execErr.equals("ExecGroup-"+iteration))
                && (new String(destErr.substring(0,4)).equals("err" +
                                                              iteration)) ) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }
    public CheckAnnotations
        (ActivationID id, MarshalledObject mo)
     throws RemoteException {
        super(id,0);
    }
    public void printOut(String toPrint) {
        System.out.println(toPrint);
    }
    public void printErr(String toPrint) {
        System.err.println(toPrint);
    }
    public void shutdown() throws Exception {
        (new Thread(this,"CheckAnnotations")).start();
    }
    public void run() {
        ActivationLibrary.deactivate(this, getID());
    }
}
